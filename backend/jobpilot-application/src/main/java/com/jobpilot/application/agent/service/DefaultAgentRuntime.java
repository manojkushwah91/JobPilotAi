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
    private final NotificationPort notificationPort;
    private final CareerAgentBrain brain;

    private final Map<UUID, Boolean> runningMissions = new ConcurrentHashMap<>();
    private volatile boolean agentRunning = false;

    public DefaultAgentRuntime(MissionService missionService,
                                AgentTaskService taskService,
                                AgentMemoryService memoryService,
                                ObservationRepository observationRepository,
                                DecisionRepository decisionRepository,
                                ToolRegistry toolRegistry,
                                NotificationPort notificationPort,
                                CareerAgentBrain brain) {
        this.missionService = missionService;
        this.taskService = taskService;
        this.memoryService = memoryService;
        this.observationRepository = observationRepository;
        this.decisionRepository = decisionRepository;
        this.toolRegistry = toolRegistry;
        this.notificationPort = notificationPort;
        this.brain = brain;
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
            var cycleCount = 0;

            while (runningMissions.getOrDefault(missionId, false)) {
                mission = missionService.getMission(missionId);

                if (mission.shouldStop()) {
                    log.info("Mission {} should stop", missionId);
                    break;
                }

                executeAgentLoop(mission);

                cycleCount++;
                if (cycleCount % 100 == 0) {
                    log.info("Mission {} completed {} cycles", missionId, cycleCount);
                }

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
        var userId = mission.userId();

        // Layer 1+2: Load identity + memory (handled by brain internally)
        brain.getOrCreateState(userId);

        // Observe current state
        observe(mission);

        // Layer 3: Think (LLM-based reasoning)
        var observations = observationRepository.findByMissionId(mission.missionId().value());
        var thinking = brain.think(userId, mission, observations);

        if (thinking.shouldAct()) {
            // Layer 4: Plan
            var plan = brain.plan(userId, mission, thinking);
            log.info("Plan: {} ({} actions)", plan.strategyNarrative(), plan.actions().size());

            // Layer 5: Execute (reflection happens per-task inside execute)
            execute(mission, plan);
        }

        checkEmails(mission);
        verify(mission);

        var review = brain.weeklyReview(userId);
        review.ifPresent(r -> {
            log.info("Weekly review: {} insights", r.insights().size());
            notificationPort.notifyUser(userId,
                "Weekly Career Review",
                "I've completed my weekly self-evaluation. " + r.insights().size() + " insights found.",
                "agent");
        });
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

    private void execute(Mission mission, AgentPlan plan) {
        log.debug("Executing plan for mission {}", mission.missionId());

        if (!plan.pendingActions().isEmpty()) {
            var missionTasks = taskService.getMissionTasks(mission.missionId().value());

            for (var action : plan.pendingActions()) {
                var taskType = inferTaskType(action.actionType());
                var alreadyPending = missionTasks.stream()
                    .anyMatch(t -> t.taskType() == taskType && t.status() == TaskStatus.PENDING);
                if (alreadyPending) continue;

                taskService.createTaskWithInput(
                    mission.missionId().value(),
                    mission.userId(),
                    taskType,
                    action.description(),
                    action.priority(),
                    action.input()
                );
                log.info("Created {} task from plan: {}", taskType, action.description());
            }
        }

        var pendingTasks = taskService.getPendingTasks();
        for (var task : pendingTasks) {
            try {
                taskService.startTask(task.taskId().value());
                var tool = toolRegistry.findByName(task.taskType().name());
                if (tool.isPresent()) {
                    Map<String, Object> taskInput = task.input() != null ? task.input() : Collections.emptyMap();
                    var enrichedInput = new LinkedHashMap<>(taskInput);

                    enrichTaskInput(mission, task, enrichedInput);

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

                    if (task.taskType() == TaskType.SUBMIT_APPLICATION) {
                        mission.incrementApplicationsSubmitted();
                        var state = brain.getOrCreateState(mission.userId());
                        state.incrementApplications();
                    }

                    var reflection = brain.reflect(mission.userId(), task, result);
                    if (!reflection.success()) {
                        mission.incrementRejected();
                    }

                } else {
                    taskService.failTask(task.taskId().value(), "Tool not found: " + task.taskType().name());
                }
            } catch (Exception e) {
                log.error("Task {} failed: {}", task.taskId(), e.getMessage());
                taskService.failTask(task.taskId().value(), e.getMessage());
            }
        }
    }

    private void enrichTaskInput(Mission mission, AgentTask task, LinkedHashMap<String, Object> input) {
        if (task.taskType() == TaskType.DISCOVER_JOBS) {
            input.putIfAbsent("query", mission.targetRole());
            input.putIfAbsent("location", mission.targetLocation());
            input.putIfAbsent("skills", String.join(", ", mission.preferredSkills()));
        }

        if (task.taskType() == TaskType.DISCOVER_JOBS && input.containsKey("board")) {
            input.putIfAbsent("query", mission.targetRole());
            input.putIfAbsent("location", mission.targetLocation());
        }

        if (task.taskType() == TaskType.SUBMIT_APPLICATION
                || task.taskType() == TaskType.TAILOR_RESUME
                || task.taskType() == TaskType.GENERATE_COVER_LETTER
                || task.taskType() == TaskType.RANK_JOB) {
            input.putIfAbsent("userId", mission.userId());
        }
    }

    private void verify(Mission mission) {
        log.debug("Verifying mission {}", mission.missionId());
        var retryableTasks = taskService.getRetryableTasks();
        for (var task : retryableTasks) {
            taskService.retryTask(task.taskId().value());
        }
    }

    private void checkEmails(Mission mission) {
        try {
            var tool = toolRegistry.findByName("MONITOR_EMAILS");
            if (tool.isEmpty()) return;

            var missionTasks = taskService.getMissionTasks(mission.missionId().value());
            var completedApps = missionTasks.stream()
                .filter(t -> t.taskType() == TaskType.SUBMIT_APPLICATION
                    && t.status() == TaskStatus.COMPLETED)
                .count();

            if (completedApps > 0) {
                var input = new LinkedHashMap<String, Object>();
                input.put("userId", mission.userId());
                input.put("maxEmails", 20);
                var result = tool.get().execute(input);
                log.info("Email check: {} signals found", result.getOrDefault("signalsFound", 0));
            }
        } catch (Exception e) {
            log.debug("Email check skipped: {}", e.getMessage());
        }
    }

    private TaskType inferTaskType(String actionType) {
        return switch (actionType.toUpperCase()) {
            case "SEARCH_JOBS" -> TaskType.DISCOVER_JOBS;
            case "APPLY_TO_JOBS" -> TaskType.SUBMIT_APPLICATION;
            case "TAILOR_RESUME" -> TaskType.TAILOR_RESUME;
            case "NOTIFY_USER" -> TaskType.SEND_NOTIFICATION;
            case "PREPARE_INTERVIEW" -> TaskType.DISCOVER_JOBS;
            case "CHECK_EMAIL" -> TaskType.DISCOVER_JOBS;
            default -> TaskType.DISCOVER_JOBS;
        };
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

        var state = brain.getOrCreateState(mission.userId());
        status.put("briefing", brain.generateBriefing(mission.userId()));
        status.put("totalApplications", state.totalApplicationsSubmitted());
        status.put("totalInterviews", state.totalInterviewsScheduled());
        status.put("consecutiveFailures", state.consecutiveFailures());
        status.put("currentPlan", state.currentPlan().toPromptContext());

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
}
