package com.jobpilot.interfaces.rest.v1.agent;

import com.jobpilot.application.agent.service.AgentChatService;
import com.jobpilot.application.agent.service.AgentTaskService;
import com.jobpilot.application.agent.service.MissionService;
import com.jobpilot.domain.agent.MissionStatus;
import com.jobpilot.infrastructure.automation.BrowserAutomationService;
import com.jobpilot.infrastructure.automation.progress.AutomationProgressTracker;
import com.jobpilot.infrastructure.automation.queue.ApplicationQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final MissionService missionService;
    private final AgentTaskService taskService;
    private final AgentChatService chatService;
    private final BrowserAutomationService automationService;
    private final AutomationProgressTracker progressTracker;
    private final ApplicationQueue applicationQueue;

    public AgentController(MissionService missionService,
                           AgentTaskService taskService,
                           AgentChatService chatService,
                           BrowserAutomationService automationService,
                           AutomationProgressTracker progressTracker,
                           ApplicationQueue applicationQueue) {
        this.missionService = missionService;
        this.taskService = taskService;
        this.chatService = chatService;
        this.automationService = automationService;
        this.progressTracker = progressTracker;
        this.applicationQueue = applicationQueue;
    }

    @PostMapping("/missions")
    public ResponseEntity<MissionResponse> createMission(@RequestBody CreateMissionRequest request) {
        var mission = missionService.createMission(
            UUID.fromString(request.userId()),
            request.title(),
            request.targetRole(),
            request.targetLocation(),
            request.salaryMin(),
            request.salaryMax(),
            request.preferredCompanies(),
            request.avoidCompanies(),
            request.preferredSkills(),
            request.experienceLevel(),
            request.employmentType(),
            request.dailyLimit(),
            request.deadlineDays()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(MissionResponse.from(mission));
    }

    @GetMapping("/missions/{missionId}")
    public ResponseEntity<MissionResponse> getMission(@PathVariable UUID missionId) {
        var mission = missionService.getMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @GetMapping("/missions/user/{userId}")
    public ResponseEntity<?> getUserMissions(@PathVariable UUID userId) {
        var missions = missionService.getUserMissions(userId);
        return ResponseEntity.ok(missions.stream().map(MissionResponse::from).toList());
    }

    @PostMapping("/missions/{missionId}/start")
    public ResponseEntity<MissionResponse> startMission(@PathVariable UUID missionId) {
        var mission = missionService.startMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @PostMapping("/missions/{missionId}/pause")
    public ResponseEntity<MissionResponse> pauseMission(@PathVariable UUID missionId) {
        var mission = missionService.pauseMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @PostMapping("/missions/{missionId}/resume")
    public ResponseEntity<MissionResponse> resumeMission(@PathVariable UUID missionId) {
        var mission = missionService.resumeMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @PostMapping("/missions/{missionId}/cancel")
    public ResponseEntity<MissionResponse> cancelMission(@PathVariable UUID missionId) {
        var mission = missionService.cancelMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @GetMapping("/tasks/pending")
    public ResponseEntity<?> getPendingTasks() {
        var tasks = taskService.getPendingTasks();
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).toList());
    }

    @PostMapping("/tasks/{taskId}/start")
    public ResponseEntity<TaskResponse> startTask(@PathVariable UUID taskId) {
        var task = taskService.startTask(taskId);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable UUID taskId,
                                                      @RequestBody Map<String, Object> output) {
        var task = taskService.completeTask(taskId, output);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/fail")
    public ResponseEntity<TaskResponse> failTask(@PathVariable UUID taskId,
                                                  @RequestBody Map<String, String> request) {
        var task = taskService.failTask(taskId, request.get("error"));
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        var userId = UUID.fromString(request.userId());
        var response = chatService.processMessage(userId, request.message());
        return ResponseEntity.ok(new ChatResponse(response.response(), response.type()));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAgentStatus() {
        var missions = missionService.getUserMissions(UUID.randomUUID());
        var activeMissions = missions.stream()
            .filter(m -> m.status() == MissionStatus.ACTIVE)
            .count();

        return ResponseEntity.ok(Map.of(
            "status", activeMissions > 0 ? "running" : "idle",
            "version", "2.0.0",
            "activeMissions", activeMissions,
            "automationRunning", automationService.isRunning(),
            "queueSize", automationService.getQueueSize(),
            "availableBoards", automationService.getAvailableBoards()
        ));
    }

    @PostMapping("/automate/start")
    public ResponseEntity<Map<String, Object>> startAutomation(@RequestBody StartAutomationRequest request) {
        var userId = UUID.fromString(request.userId());
        var missionId = request.missionId() != null ? UUID.fromString(request.missionId()) : null;

        log.info("Starting automation: board={} user={}", request.boardName(), userId);

        var headless = request.headless() != null ? request.headless() : true;

        automationService.runAutomation(
            request.boardName(),
            request.credentials(),
            userId,
            missionId,
            headless
        );

        return ResponseEntity.accepted().body(Map.of(
            "status", "started",
            "boardName", request.boardName(),
            "headless", headless,
            "queueSize", automationService.getQueueSize()
        ));
    }

    @PostMapping("/automate/stop")
    public ResponseEntity<Map<String, String>> stopAutomation() {
        automationService.stop();
        return ResponseEntity.ok(Map.of("status", "stopping"));
    }

    @GetMapping("/automate/status")
    public ResponseEntity<Map<String, Object>> getAutomationStatus() {
        return ResponseEntity.ok(Map.of(
            "running", automationService.isRunning(),
            "currentSessionId", automationService.getCurrentSessionId() != null ?
                automationService.getCurrentSessionId() : "none",
            "queueSize", automationService.getQueueSize(),
            "availableBoards", automationService.getAvailableBoards()
        ));
    }

    @GetMapping("/automate/boards")
    public ResponseEntity<Map<String, Object>> getAvailableBoards() {
        return ResponseEntity.ok(Map.of(
            "boards", automationService.getAvailableBoards()
        ));
    }

    @PostMapping("/automate/queue")
    public ResponseEntity<Map<String, Object>> addToQueue(@RequestBody QueueJobRequest request) {
        var added = applicationQueue.enqueue(new ApplicationQueue.JobApplicationRequest(
            request.jobUrl(),
            request.boardName(),
            request.jobTitle(),
            request.companyName(),
            request.userProfile() != null ? request.userProfile() : Map.of()
        ));

        return ResponseEntity.ok(Map.of(
            "added", added,
            "queueSize", applicationQueue.size()
        ));
    }

    @GetMapping("/automate/progress")
    public ResponseEntity<?> getAllProgress() {
        var allProgress = progressTracker.getAllProgress();
        return ResponseEntity.ok(allProgress);
    }

    @GetMapping("/automate/progress/{sessionId}")
    public ResponseEntity<?> getSessionProgress(@PathVariable String sessionId) {
        var progress = progressTracker.getProgress(sessionId);
        if (progress.isPresent()) {
            return ResponseEntity.ok(progress.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/automate/proxy")
    public ResponseEntity<Map<String, Object>> getProxyStats() {
        return ResponseEntity.ok(automationService.getProxyStats());
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<Map<String, Object>> getAnalyticsOverview() {
        var totalSubmitted = automationService.getResultsByOutcome("SUBMITTED").size();
        var totalFailed = automationService.getResultsByOutcome("FAILED").size();
        var totalPending = automationService.getResultsByOutcome("PENDING_CAPTCHA").size();
        var total = totalSubmitted + totalFailed + totalPending;
        var successRate = total > 0 ? (double) totalSubmitted / total * 100 : 0.0;

        return ResponseEntity.ok(Map.of(
            "totalApplications", total,
            "submitted", totalSubmitted,
            "failed", totalFailed,
            "pendingCaptcha", totalPending,
            "successRate", Math.round(successRate * 100.0) / 100.0,
            "availableBoards", automationService.getAvailableBoards(),
            "automationRunning", automationService.isRunning(),
            "queueSize", automationService.getQueueSize()
        ));
    }

    public record ChatRequest(String userId, String message) {}
    public record ChatResponse(String response, String type) {}

    public record StartAutomationRequest(
        String boardName,
        Map<String, String> credentials,
        String userId,
        String missionId,
        Boolean headless
    ) {}

    public record QueueJobRequest(
        String jobUrl,
        String boardName,
        String jobTitle,
        String companyName,
        Map<String, String> userProfile
    ) {}
}
