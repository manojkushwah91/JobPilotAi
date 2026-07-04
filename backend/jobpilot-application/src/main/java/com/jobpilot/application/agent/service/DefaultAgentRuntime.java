package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.*;
import com.jobpilot.domain.agent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (!mission.hasReachedDailyLimit()) {
            var discoveryTask = AgentTask.create(
                mission.missionId().value(),
                mission.userId(),
                TaskType.DISCOVER_JOBS,
                "Discover new jobs matching mission criteria"
            );
            taskService.createTaskWithPriority(
                mission.missionId().value(),
                mission.userId(),
                TaskType.DISCOVER_JOBS,
                "Discover new jobs",
                10
            );
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
                    var result = tool.get().execute(task.input() != null ? task.input() : Map.of());
                    taskService.completeTask(task.taskId().value(), result);
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
}
