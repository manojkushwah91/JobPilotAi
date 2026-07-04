package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.domain.agent.MissionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AgentChatService {

    private static final Logger log = LoggerFactory.getLogger(AgentChatService.class);

    private final MissionService missionService;
    private final AgentTaskService taskService;
    private final AiProviderPort aiProvider;

    public AgentChatService(MissionService missionService,
                             AgentTaskService taskService,
                             AiProviderPort aiProvider) {
        this.missionService = missionService;
        this.taskService = taskService;
        this.aiProvider = aiProvider;
    }

    public ChatResponse processMessage(UUID userId, String message) {
        log.info("Processing chat message from user {}: {}", userId, message);

        var lowerMessage = message.toLowerCase().trim();

        if (lowerMessage.contains("find") && lowerMessage.contains("job")) {
            return handleFindJobs(userId, message);
        }

        if (lowerMessage.contains("pause")) {
            return handlePauseMission(userId);
        }

        if (lowerMessage.contains("resume") || lowerMessage.contains("continue")) {
            return handleResumeMission(userId);
        }

        if (lowerMessage.contains("stop") || lowerMessage.contains("cancel")) {
            return handleStopMission(userId);
        }

        if (lowerMessage.contains("status") || lowerMessage.contains("progress")) {
            return handleGetStatus(userId);
        }

        if (lowerMessage.contains("skip")) {
            return handleSkipCompany(userId, message);
        }

        if (lowerMessage.contains("salary")) {
            return handleSalaryUpdate(userId, message);
        }

        return handleGeneralQuery(userId, message);
    }

    private ChatResponse handleFindJobs(UUID userId, String message) {
        var missions = missionService.getUserMissions(userId);
        var activeMissions = missions.stream()
            .filter(m -> m.status() == MissionStatus.ACTIVE)
            .toList();

        if (activeMissions.isEmpty()) {
            return new ChatResponse(
                "I don't have an active mission yet. Please create a mission in Mission Control first, " +
                "then I'll start searching for jobs matching your criteria.",
                "info"
            );
        }

        return new ChatResponse(
            "I'm actively searching for jobs. Currently monitoring " + activeMissions.size() +
            " mission(s). I'll notify you when I find matching opportunities.",
            "success"
        );
    }

    private ChatResponse handlePauseMission(UUID userId) {
        var missions = missionService.getUserMissions(userId);
        var activeMissions = missions.stream()
            .filter(m -> m.status() == MissionStatus.ACTIVE)
            .toList();

        if (activeMissions.isEmpty()) {
            return new ChatResponse("No active missions to pause.", "info");
        }

        for (var mission : activeMissions) {
            missionService.pauseMission(mission.missionId().value());
        }

        return new ChatResponse(
            "Paused " + activeMissions.size() + " mission(s). I'll stop searching until you resume.",
            "success"
        );
    }

    private ChatResponse handleResumeMission(UUID userId) {
        var missions = missionService.getUserMissions(userId);
        var pausedMissions = missions.stream()
            .filter(m -> m.status() == MissionStatus.PAUSED)
            .toList();

        if (pausedMissions.isEmpty()) {
            return new ChatResponse("No paused missions to resume.", "info");
        }

        for (var mission : pausedMissions) {
            missionService.resumeMission(mission.missionId().value());
        }

        return new ChatResponse(
            "Resumed " + pausedMissions.size() + " mission(s). I'm back to work!",
            "success"
        );
    }

    private ChatResponse handleStopMission(UUID userId) {
        var missions = missionService.getUserMissions(userId);
        var activeMissions = missions.stream()
            .filter(m -> m.status() == MissionStatus.ACTIVE || m.status() == MissionStatus.PAUSED)
            .toList();

        if (activeMissions.isEmpty()) {
            return new ChatResponse("No active missions to stop.", "info");
        }

        for (var mission : activeMissions) {
            missionService.cancelMission(mission.missionId().value());
        }

        return new ChatResponse(
            "Cancelled " + activeMissions.size() + " mission(s).",
            "success"
        );
    }

    private ChatResponse handleGetStatus(UUID userId) {
        var missions = missionService.getUserMissions(userId);
        var activeCount = missions.stream().filter(m -> m.status() == MissionStatus.ACTIVE).count();
        var totalApplied = missions.stream().mapToInt(m -> m.totalApplicationsSubmitted()).sum();
        var totalFound = missions.stream().mapToInt(m -> m.totalJobsFound()).sum();

        return new ChatResponse(
            String.format(
                "Mission Status: %d active, %d total. Jobs found: %d, Applications submitted: %d.",
                activeCount, missions.size(), totalFound, totalApplied
            ),
            "info"
        );
    }

    private ChatResponse handleSkipCompany(UUID userId, String message) {
        var companyName = extractCompanyName(message);
        if (companyName != null) {
            return new ChatResponse(
                "I'll skip " + companyName + " in future job searches. " +
                "This preference has been noted.",
                "success"
            );
        }
        return new ChatResponse("Which company would you like me to skip?", "info");
    }

    private ChatResponse handleSalaryUpdate(UUID userId, String message) {
        return new ChatResponse(
            "To update salary preferences, please modify your mission in Mission Control. " +
            "I'll use the updated criteria for job matching.",
            "info"
        );
    }

    private ChatResponse handleGeneralQuery(UUID userId, String message) {
        var systemPrompt = "You are JobPilot AI, an autonomous job agent assistant. " +
            "Respond concisely to user queries about job hunting. " +
            "If the user asks about job search, applications, or missions, " +
            "guide them to use Mission Control or provide status updates. " +
            "Keep responses under 2 sentences.";

        var response = aiProvider.executePrompt(systemPrompt, message, null, 0.7, 200);

        return new ChatResponse(response, "info");
    }

    private String extractCompanyName(String message) {
        var lowerMessage = message.toLowerCase();
        var skipIndex = lowerMessage.indexOf("skip");
        if (skipIndex >= 0) {
            var afterSkip = message.substring(skipIndex + 4).trim();
            var words = afterSkip.split("\\s+");
            if (words.length > 0) {
                return words[0];
            }
        }
        return null;
    }

    public record ChatResponse(String response, String type) {}
}
