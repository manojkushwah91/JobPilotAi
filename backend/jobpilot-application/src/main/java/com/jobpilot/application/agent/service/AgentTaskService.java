package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.AgentTaskRepository;
import com.jobpilot.domain.agent.AgentTask;
import com.jobpilot.domain.agent.TaskStatus;
import com.jobpilot.domain.agent.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AgentTaskService {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskService.class);

    private final AgentTaskRepository taskRepository;

    public AgentTaskService(AgentTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public AgentTask createTask(UUID missionId, UUID userId, TaskType taskType, String description) {
        var task = AgentTask.create(missionId, userId, taskType, description);
        taskRepository.save(task);
        log.info("Created task {} for mission {}", task.taskId(), missionId);
        return task;
    }

    public AgentTask createTaskWithPriority(UUID missionId, UUID userId, TaskType taskType,
                                             String description, int priority) {
        var task = AgentTask.createWithPriority(missionId, userId, taskType, description, priority);
        taskRepository.save(task);
        log.info("Created task {} with priority {} for mission {}", task.taskId(), priority, missionId);
        return task;
    }

    public AgentTask getTask(UUID taskId) {
        var taskIdObj = com.jobpilot.domain.agent.AgentTaskId.from(taskId);
        return taskRepository.findById(taskIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    public List<AgentTask> getMissionTasks(UUID missionId) {
        return taskRepository.findByMissionId(missionId);
    }

    public List<AgentTask> getPendingTasks() {
        return taskRepository.findPendingTasks(100);
    }

    public List<AgentTask> getRetryableTasks() {
        return taskRepository.findFailedTasksThatCanRetry(50);
    }

    public AgentTask startTask(UUID taskId) {
        var task = getTask(taskId);
        task.start();
        taskRepository.save(task);
        return task;
    }

    public AgentTask completeTask(UUID taskId, Map<String, Object> output) {
        var task = getTask(taskId);
        task.complete(output);
        taskRepository.save(task);
        log.info("Completed task {}", taskId);
        return task;
    }

    public AgentTask failTask(UUID taskId, String error) {
        var task = getTask(taskId);
        task.fail(error);
        taskRepository.save(task);
        log.warn("Failed task {}: {}", taskId, error);
        return task;
    }

    public AgentTask retryTask(UUID taskId) {
        var task = getTask(taskId);
        if (task.canRetry()) {
            task.incrementRetry();
            taskRepository.save(task);
            log.info("Retrying task {} (attempt {})", taskId, task.retryCount());
        }
        return task;
    }
}
