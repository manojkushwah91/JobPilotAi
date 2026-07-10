package com.jobpilot.interfaces.rest.v1.agent;

import com.jobpilot.domain.agent.AgentRuntime;
import com.jobpilot.application.agent.service.AgentChatService;
import com.jobpilot.application.agent.service.AgentTaskService;
import com.jobpilot.application.agent.service.CareerAgentBrain;
import com.jobpilot.application.agent.service.MissionService;
import com.jobpilot.domain.agent.MissionStatus;
import com.jobpilot.infrastructure.automation.BrowserAutomationService;
import com.jobpilot.infrastructure.automation.email.EmailMonitorService;
import com.jobpilot.infrastructure.automation.progress.AutomationProgressTracker;
import com.jobpilot.infrastructure.automation.queue.ApplicationQueue;
import com.jobpilot.infrastructure.automation.ratelimit.BoardRateLimiter;
import com.jobpilot.infrastructure.automation.screenshot.ScreenshotDiffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final MissionService missionService;
    private final AgentTaskService taskService;
    private final AgentChatService chatService;
    private final AgentRuntime agentRuntime;
    private final CareerAgentBrain careerAgentBrain;
    private final BrowserAutomationService automationService;
    private final AutomationProgressTracker progressTracker;
    private final ApplicationQueue applicationQueue;
    private final EmailMonitorService emailMonitorService;
    private final BoardRateLimiter rateLimiter;
    private final ScreenshotDiffer screenshotDiffer;

    public AgentController(MissionService missionService,
                           AgentTaskService taskService,
                           AgentChatService chatService,
                           AgentRuntime agentRuntime,
                           CareerAgentBrain careerAgentBrain,
                           BrowserAutomationService automationService,
                           AutomationProgressTracker progressTracker,
                           ApplicationQueue applicationQueue,
                           EmailMonitorService emailMonitorService,
                           BoardRateLimiter rateLimiter,
                           ScreenshotDiffer screenshotDiffer) {
        this.missionService = missionService;
        this.taskService = taskService;
        this.chatService = chatService;
        this.agentRuntime = agentRuntime;
        this.careerAgentBrain = careerAgentBrain;
        this.automationService = automationService;
        this.progressTracker = progressTracker;
        this.applicationQueue = applicationQueue;
        this.emailMonitorService = emailMonitorService;
        this.rateLimiter = rateLimiter;
        this.screenshotDiffer = screenshotDiffer;
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

    @GetMapping("/missions/{missionId}/status")
    public ResponseEntity<Map<String, Object>> getMissionStatus(@PathVariable UUID missionId) {
        var status = agentRuntime.getMissionStatus(missionId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/briefing")
    public ResponseEntity<Map<String, Object>> getBriefing(@RequestParam UUID userId) {
        var briefing = careerAgentBrain.generateBriefing(userId);
        var state = careerAgentBrain.getOrCreateState(userId);
        var review = careerAgentBrain.weeklyReview(userId);
        return ResponseEntity.ok(Map.of(
            "briefing", briefing,
            "totalApplications", state.totalApplicationsSubmitted(),
            "totalInterviews", state.totalInterviewsScheduled(),
            "consecutiveFailures", state.consecutiveFailures(),
            "currentPlan", state.currentPlan().toPromptContext(),
            "latestWeeklyReview", review.map(r -> r.toBriefing()).orElse("")
        ));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations(@RequestParam UUID userId) {
        return ResponseEntity.ok(careerAgentBrain.getRecommendations(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCareerHealth(@RequestParam UUID userId) {
        return ResponseEntity.ok(careerAgentBrain.getCareerHealth(userId));
    }

    @GetMapping("/attention")
    public ResponseEntity<List<Map<String, Object>>> getRequiresAttention(@RequestParam UUID userId) {
        return ResponseEntity.ok(careerAgentBrain.getRequiresAttention(userId));
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateAgentSettings(
            @RequestParam UUID userId,
            @RequestBody Map<String, Object> body) {
        careerAgentBrain.updateAgentSettings(userId,
            (String) body.get("preferredLocation"),
            body.get("salaryMin") != null ? ((Number) body.get("salaryMin")).intValue() : null,
            body.get("salaryMax") != null ? ((Number) body.get("salaryMax")).intValue() : null,
            castStringList(body.get("preferredCompanies")),
            castStringList(body.get("avoidCompanies")),
            (String) body.get("employmentType"),
            (String) body.get("workAuthorization"),
            (String) body.get("careerGoal"),
            castStringList(body.get("certifications")),
            castStringList(body.get("projects"))
        );
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object value) {
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return null;
    }

    @PostMapping("/missions/{missionId}/start")
    public ResponseEntity<MissionResponse> startMission(@PathVariable UUID missionId) {
        agentRuntime.startMission(missionId);
        var mission = missionService.getMission(missionId);
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
    public ResponseEntity<Map<String, Object>> getAgentStatus(@RequestParam(required = false) UUID userId) {
        var missions = userId != null
            ? missionService.getUserMissions(userId)
            : java.util.Collections.<com.jobpilot.domain.agent.Mission>emptyList();
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

    @GetMapping("/automate/results")
    public ResponseEntity<?> getAutomationResults() {
        var results = automationService.getRecentResults();
        return ResponseEntity.ok(results);
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

    @GetMapping("/email/events")
    public ResponseEntity<?> getEmailEvents() {
        return ResponseEntity.ok(emailMonitorService.getRecentEvents());
    }

    @GetMapping("/email/events/counts")
    public ResponseEntity<?> getEmailEventCounts() {
        return ResponseEntity.ok(emailMonitorService.getEventCounts());
    }

    @PostMapping("/email/process")
    public ResponseEntity<?> processEmail(@RequestBody ProcessEmailRequest request) {
        var event = emailMonitorService.processIncomingEmail(
            request.messageId(),
            request.senderEmail(),
            request.subject(),
            request.body(),
            UUID.fromString(request.userId())
        );
        return ResponseEntity.ok(Map.of(
            "processed", event != null,
            "eventType", event != null ? event.eventType().name() : "IGNORED"
        ));
    }

    @GetMapping("/ratelimit/stats")
    public ResponseEntity<?> getRateLimitStats() {
        var stats = new java.util.HashMap<String, Object>();
        for (var board : automationService.getAvailableBoards()) {
            stats.put(board, rateLimiter.getStats(board).toMap());
        }
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/ratelimit/limits")
    public ResponseEntity<?> setRateLimits(@RequestBody RateLimitRequest request) {
        rateLimiter.setLimits(request.boardName(), request.delayMs(), request.dailyCap(), request.hourlyCap());
        return ResponseEntity.ok(Map.of("status", "updated", "board", request.boardName()));
    }

    @PostMapping("/screenshot/compare")
    public ResponseEntity<?> compareScreenshots(@RequestBody ScreenshotCompareRequest request) {
        var before = screenshotDiffer.fromBase64(request.beforeBase64());
        var after = screenshotDiffer.fromBase64(request.afterBase64());
        var result = screenshotDiffer.compareScreenshots(before, after);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/automate/captcha/solve")
    public ResponseEntity<Map<String, Object>> solveCaptcha(@RequestBody CaptchaSolveRequest request) {
        var resolved = automationService.resolveCaptcha(request.sessionId(), request.solution());
        return ResponseEntity.ok(Map.of(
            "resolved", resolved,
            "sessionId", request.sessionId()
        ));
    }

    public record ProcessEmailRequest(
        String messageId, String senderEmail, String subject,
        String body, String userId
    ) {}

    public record RateLimitRequest(
        String boardName, int delayMs, int dailyCap, int hourlyCap
    ) {}

    public record ScreenshotCompareRequest(
        String beforeBase64, String afterBase64
    ) {}

    public record CaptchaSolveRequest(String sessionId, String solution) {}

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
