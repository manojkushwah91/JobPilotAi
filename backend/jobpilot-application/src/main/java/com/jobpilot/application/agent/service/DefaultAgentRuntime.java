package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.*;
import com.jobpilot.domain.agent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultAgentRuntime implements AgentRuntime {

    private static final Logger log = LoggerFactory.getLogger(DefaultAgentRuntime.class);

    private final MissionService missionService;
    private final AgentTaskService taskService;
    private final AgentMemoryService memoryService;
    private final ObservationRepository observationRepository;
    private final DecisionRepository decisionRepository;
    private final ToolRegistry toolRegistry;
    private final AiProviderPort aiProvider;
    private final NotificationPort notificationPort;

    private final Map<UUID, Boolean> runningMissions = new ConcurrentHashMap<>();
    private volatile boolean agentRunning = false;

    public DefaultAgentRuntime(MissionService missionService,
                                AgentTaskService taskService,
                                AgentMemoryService memoryService,
                                ObservationRepository observationRepository,
                                DecisionRepository decisionRepository,
                                ToolRegistry toolRegistry,
                                AiProviderPort aiProvider,
                                NotificationPort notificationPort) {
        this.missionService = missionService;
        this.taskService = taskService;
        this.memoryService = memoryService;
        this.observationRepository = observationRepository;
        this.decisionRepository = decisionRepository;
        this.toolRegistry = toolRegistry;
        this.aiProvider = aiProvider;
        this.notificationPort = notificationPort;
    }

    @Override
    @Async
    public void startMission(UUID missionId) {
        log.info("Starting agent loop for mission {}", missionId);
        runningMissions.put(missionId, true);
        agentRunning = true;

        try {
            var mission = missionService.getMission(missionId);
            missionService.startMission(missionId);

            while (runningMissions.getOrDefault(missionId, false)) {
                mission = missionService.getMission(missionId);

                if (mission.shouldStop()) {
                    log.info("Mission {} should stop", missionId);
                    break;
                }

                executeAgentLoop(mission);

                Thread.sleep(5000);
            }

            mission = missionService.getMission(missionId);
            if (mission.status() == MissionStatus.ACTIVE) {
                missionService.pauseMission(missionId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Agent loop interrupted for mission {}", missionId);
        } catch (Exception e) {
            log.error("Agent loop failed for mission {}: {}", missionId, e.getMessage(), e);
        } finally {
            runningMissions.remove(missionId);
            if (runningMissions.isEmpty()) {
                agentRunning = false;
            }
        }
    }

    private void executeAgentLoop(Mission mission) throws Exception {
        observe(mission);
        var observations = observationRepository.findByMissionId(mission.missionId().value());
        var decision = reason(mission, observations);
        if (decision != null) {
            var plan = plan(mission, decision);
            execute(mission, plan);
        }
        verify(mission);
        learn(mission);
    }

    private void observe(Mission mission) {
        log.debug("Observing for mission {}", mission.missionId());
        var observation = AgentObservation.create(
            mission.missionId().value(),
            ObservationType.MISSION_PROGRESS,
            "agent_runtime",
            "Mission status check"
        );
        observationRepository.save(observation);
    }

    @SuppressWarnings("unchecked")
    private AgentDecision reason(Mission mission, List<AgentObservation> observations) {
        log.debug("Reasoning for mission {}", mission.missionId());

        var pendingTasks = taskService.getPendingTasks();
        if (!pendingTasks.isEmpty()) {
            return AgentDecision.create(
                mission.missionId().value(),
                DecisionType.APPLY_TO_JOB,
                "Pending tasks available for execution"
            );
        }

        var missionTasks = taskService.getMissionTasks(mission.missionId().value());

        var hasRunningApplyTasks = missionTasks.stream()
            .anyMatch(t -> t.taskType() == TaskType.SUBMIT_APPLICATION
                && (t.status() == TaskStatus.RUNNING || t.status() == TaskStatus.PENDING));

        if (hasRunningApplyTasks) {
            log.debug("Mission {} has running apply tasks, skipping discovery", mission.missionId());
            return null;
        }

        var unprocessedDiscoveries = missionTasks.stream()
            .filter(t -> t.taskType() == TaskType.DISCOVER_JOBS
                && t.status() == TaskStatus.COMPLETED
                && t.output() != null
                && !t.output().containsKey("processed"))
            .toList();

        if (!unprocessedDiscoveries.isEmpty()) {
            for (var discoveryTask : unprocessedDiscoveries) {
                var jobsFound = discoveryTask.output().get("jobsFound");
                if (jobsFound instanceof List<?> jobs && !jobs.isEmpty()) {
                    log.info("Processing {} discovered jobs from task {}", jobs.size(), discoveryTask.taskId());

                    int remaining = (int) mission.dailyApplicationLimit() -
                        mission.totalApplicationsSubmitted();

                    for (int i = 0; i < Math.min(jobs.size(), remaining); i++) {
                        var job = (Map<String, Object>) jobs.get(i);
                        var jobUrl = (String) job.getOrDefault("url", "");
                        var jobTitle = (String) job.getOrDefault("title", "Unknown");
                        var company = (String) job.getOrDefault("company", "Unknown");
                        var description = (String) job.getOrDefault("description", "");

                        if (jobUrl.isBlank()) continue;

                        var scoreResult = scoreJob(mission, jobTitle, company, description);
                        var score = scoreResult != null ? ((Number) scoreResult.getOrDefault("score", 0)).intValue() : 50;
                        var recommendation = scoreResult != null ? (String) scoreResult.getOrDefault("recommendation", "maybe") : "maybe";

                        log.info("Job score: {} at {} = {}/100 ({})", jobTitle, company, score, recommendation);

                        if (score < 50 && "skip".equals(recommendation)) {
                            log.info("Skipping low-score job: {} at {} (score: {})", jobTitle, company, score);
                            continue;
                        }

                        var input = new LinkedHashMap<String, Object>();
                        input.put("url", jobUrl);
                        input.put("title", jobTitle);
                        input.put("company", company);
                        input.put("jobId", job.get("id"));
                        input.put("description", description);
                        input.put("matchScore", score);
                        input.put("matchRecommendation", recommendation);

                        if (score >= 60) {
                            var tailored = tailorResume(mission, jobTitle, company, description);
                            if (tailored != null && "success".equals(tailored.get("status"))) {
                                input.put("tailoredResume", tailored.get("tailoredResume"));
                            }

                            var coverLetter = generateCoverLetter(mission, jobTitle, company, description);
                            if (coverLetter != null && "success".equals(coverLetter.get("status"))) {
                                input.put("coverLetter", coverLetter.get("coverLetter"));
                            }
                        }

                        taskService.createTaskWithInput(
                            mission.missionId().value(),
                            mission.userId(),
                            TaskType.SUBMIT_APPLICATION,
                            "Apply to: " + jobTitle + " at " + company,
                            5,
                            input
                        );
                        log.info("Created SUBMIT_APPLICATION task for {} at {} (score: {})", jobTitle, company, score);
                    }

                    discoveryTask.output().put("processed", true);
                }
            }

            return AgentDecision.create(
                mission.missionId().value(),
                DecisionType.APPLY_TO_JOB,
                "Created apply tasks from discovered jobs"
            );
        }

        if (!mission.hasReachedDailyLimit()) {
            var hasRecentDiscovery = missionTasks.stream()
                .anyMatch(t -> t.taskType() == TaskType.DISCOVER_JOBS
                    && t.status() == TaskStatus.COMPLETED);

            if (!hasRecentDiscovery) {
                taskService.createTaskWithPriority(
                    mission.missionId().value(),
                    mission.userId(),
                    TaskType.DISCOVER_JOBS,
                    "Discover new jobs matching mission criteria",
                    10
                );
                log.info("Created DISCOVER_JOBS task for mission {}", mission.missionId());
            }
        }

        return null;
    }

    private Map<String, Object> plan(Mission mission, AgentDecision decision) {
        log.debug("Planning for mission {}", mission.missionId());
        var plan = new HashMap<String, Object>();
        plan.put("missionId", mission.missionId().value());
        plan.put("decisionType", decision.type().name());
        plan.put("timestamp", Instant.now().toString());
        return plan;
    }

    private void execute(Mission mission, Map<String, Object> plan) {
        log.debug("Executing plan for mission {}", mission.missionId());

        var pendingTasks = taskService.getPendingTasks();
        for (var task : pendingTasks) {
            try {
                taskService.startTask(task.taskId().value());
                var tool = toolRegistry.findByName(task.taskType().name());
                if (tool.isPresent()) {
                    Map<String, Object> taskInput = task.input() != null ? task.input() : Map.of();
                    var enrichedInput = new LinkedHashMap<String, Object>(taskInput);

                    if (task.taskType() == TaskType.DISCOVER_JOBS) {
                        enrichedInput.putIfAbsent("query", mission.targetRole());
                        enrichedInput.putIfAbsent("location", mission.targetLocation());
                        enrichedInput.putIfAbsent("skills", String.join(", ", mission.preferredSkills()));
                    }

                    if (task.taskType() == TaskType.SUBMIT_APPLICATION) {
                        enrichedInput.putIfAbsent("userId", mission.userId());
                    }

                    if (task.taskType() == TaskType.TAILOR_RESUME
                            || task.taskType() == TaskType.GENERATE_COVER_LETTER
                            || task.taskType() == TaskType.RANK_JOB) {
                        enrichedInput.putIfAbsent("userId", mission.userId());
                    }

                    var result = tool.get().execute(enrichedInput);

                    if (task.taskType() == TaskType.SUBMIT_APPLICATION
                            && "captcha_required".equals(result.get("status"))) {
                        notificationPort.notifyUser(mission.userId(),
                            "CAPTCHA Detected",
                            "CAPTCHA detected on " + result.getOrDefault("url", "unknown job")
                                + ". Manual intervention required.",
                            "agent");
                    }

                    taskService.completeTask(task.taskId().value(), result);
                    log.info("Task {} completed with result: {}", task.taskId(), result);
                } else {
                    taskService.failTask(task.taskId().value(), "Tool not found: " + task.taskType().name());
                }
            } catch (Exception e) {
                log.error("Task {} failed: {}", task.taskId(), e.getMessage());
                taskService.failTask(task.taskId().value(), e.getMessage());
            }
        }
    }

    private void verify(Mission mission) {
        log.debug("Verifying mission {}", mission.missionId());
        var retryableTasks = taskService.getRetryableTasks();
        for (var task : retryableTasks) {
            taskService.retryTask(task.taskId().value());
        }
    }

    private void learn(Mission mission) {
        log.debug("Learning from mission {}", mission.missionId());
        var tasks = taskService.getMissionTasks(mission.missionId().value());

        var completedApplyTasks = tasks.stream()
            .filter(t -> t.taskType() == TaskType.SUBMIT_APPLICATION && t.status() == TaskStatus.COMPLETED)
            .toList();

        var failedApplyTasks = tasks.stream()
            .filter(t -> t.taskType() == TaskType.SUBMIT_APPLICATION && t.status() == TaskStatus.FAILED)
            .toList();

        if (!completedApplyTasks.isEmpty() || !failedApplyTasks.isEmpty()) {
            var total = completedApplyTasks.size() + failedApplyTasks.size();
            var successRate = total > 0 ? (double) completedApplyTasks.size() / total : 0.0;

            memoryService.store(
                mission.userId(),
                MemoryType.SUCCESS_RATE,
                "mission:" + mission.missionId().value(),
                String.format("{\"successRate\":%.2f,\"applied\":%d,\"failed\":%d}",
                    successRate, completedApplyTasks.size(), failedApplyTasks.size())
            );
        }

        for (var task : completedApplyTasks) {
            if (task.output() != null && task.output().containsKey("company")) {
                var company = (String) task.output().get("company");
                if (company != null && !company.isBlank()) {
                    memoryService.store(
                        mission.userId(),
                        MemoryType.APPLIED_COMPANY,
                        company.toLowerCase(),
                        "Applied via mission " + mission.missionId().value()
                    );
                }
            }
        }

        for (var task : failedApplyTasks) {
            if (task.output() != null && task.output().containsKey("company")) {
                var company = (String) task.output().get("company");
                if (company != null && !company.isBlank()) {
                    memoryService.store(
                        mission.userId(),
                        MemoryType.REJECTED_COMPANY,
                        company.toLowerCase(),
                        "Failed to apply: " + (task.errorMessage() != null ? task.errorMessage() : "unknown")
                    );
                }
            }
        }
    }

    @Override
    public void pauseMission(UUID missionId) {
        runningMissions.put(missionId, false);
        missionService.pauseMission(missionId);
        log.info("Paused mission {}", missionId);
    }

    @Override
    public void resumeMission(UUID missionId) {
        startMission(missionId);
    }

    @Override
    public void stopMission(UUID missionId) {
        runningMissions.put(missionId, false);
        missionService.cancelMission(missionId);
        log.info("Stopped mission {}", missionId);
    }

    @Override
    public AgentTask executeTask(AgentTask task) {
        return taskService.startTask(task.taskId().value());
    }

    @Override
    public AgentDecision makeDecision(AgentDecision decision) {
        decisionRepository.save(decision);
        return decision;
    }

    @Override
    public AgentObservation observe(AgentObservation observation) {
        observationRepository.save(observation);
        return observation;
    }

    @Override
    public void storeMemory(AgentMemory memory) {
        memoryService.store(memory.userId(), memory.memoryType(), memory.memoryKey(), memory.value());
    }

    @Override
    public AgentMemory recallMemory(UUID userId, MemoryType type, String key) {
        return memoryService.recall(userId, type, key);
    }

    @Override
    public Map<String, Object> getMissionStatus(UUID missionId) {
        var mission = missionService.getMission(missionId);
        var tasks = taskService.getMissionTasks(missionId);
        var status = new HashMap<String, Object>();
        status.put("mission", mission);
        status.put("tasks", tasks);
        status.put("isRunning", runningMissions.getOrDefault(missionId, false));
        return status;
    }

    @Override
    public Map<String, Object> getAgentStatus() {
        var status = new HashMap<String, Object>();
        status.put("agentRunning", agentRunning);
        status.put("activeMissions", runningMissions.size());
        status.put("runningMissions", new ArrayList<>(runningMissions.keySet()));
        return status;
    }

    private Map<String, Object> scoreJob(Mission mission, String jobTitle, String company, String description) {
        try {
            var tool = toolRegistry.findByName("RANK_JOB");
            if (tool.isEmpty()) return null;
            var input = new LinkedHashMap<String, Object>();
            input.put("title", jobTitle);
            input.put("company", company);
            input.put("description", description);
            input.put("userId", mission.userId());
            return tool.get().execute(input);
        } catch (Exception e) {
            log.warn("Job scoring failed for {} at {}: {}", jobTitle, company, e.getMessage());
            return null;
        }
    }

    private Map<String, Object> tailorResume(Mission mission, String jobTitle, String company, String description) {
        try {
            var tool = toolRegistry.findByName("TAILOR_RESUME");
            if (tool.isEmpty()) return null;
            var input = new LinkedHashMap<String, Object>();
            input.put("title", jobTitle);
            input.put("company", company);
            input.put("description", description);
            input.put("userId", mission.userId());
            return tool.get().execute(input);
        } catch (Exception e) {
            log.warn("Resume tailoring failed for {} at {}: {}", jobTitle, company, e.getMessage());
            return null;
        }
    }

    private Map<String, Object> generateCoverLetter(Mission mission, String jobTitle, String company, String description) {
        try {
            var tool = toolRegistry.findByName("GENERATE_COVER_LETTER");
            if (tool.isEmpty()) return null;
            var input = new LinkedHashMap<String, Object>();
            input.put("title", jobTitle);
            input.put("company", company);
            input.put("description", description);
            input.put("userId", mission.userId());
            return tool.get().execute(input);
        } catch (Exception e) {
            log.warn("Cover letter generation failed for {} at {}: {}", jobTitle, company, e.getMessage());
            return null;
        }
    }
}
