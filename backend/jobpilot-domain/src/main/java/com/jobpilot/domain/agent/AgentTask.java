package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AgentTask extends BaseAggregateRoot {

    private AgentTaskId taskId;
    private UUID missionId;
    private UUID userId;
    private TaskType taskType;
    private TaskStatus status;
    private int priority;
    private String description;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private String errorMessage;
    private int retryCount;
    private int maxRetries;
    private Instant startedAt;
    private Instant completedAt;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant updatedAt;

    private AgentTask(AgentTaskId taskId, UUID missionId, UUID userId, TaskType taskType, String description) {
        super(taskId.value());
        this.taskId = taskId;
        this.missionId = missionId;
        this.userId = userId;
        this.taskType = taskType;
        this.status = TaskStatus.PENDING;
        this.priority = 5;
        this.description = description;
        this.retryCount = 0;
        this.maxRetries = 3;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static AgentTask create(UUID missionId, UUID userId, TaskType taskType, String description) {
        return new AgentTask(AgentTaskId.generate(), missionId, userId, taskType, description);
    }

    public static AgentTask createWithPriority(UUID missionId, UUID userId, TaskType taskType,
                                                String description, int priority) {
        var task = new AgentTask(AgentTaskId.generate(), missionId, userId, taskType, description);
        task.priority = priority;
        return task;
    }

    public static AgentTask reconstitute(AgentTaskId taskId, UUID missionId, UUID userId,
                                           TaskType taskType, TaskStatus status, int priority,
                                           String description, Map<String, Object> input,
                                           Map<String, Object> output, String errorMessage,
                                           int retryCount, int maxRetries, Instant startedAt,
                                           Instant completedAt, Instant scheduledAt,
                                           Instant createdAt, Instant updatedAt) {
        var t = new AgentTask(taskId, missionId, userId, taskType, description);
        t.status = status;
        t.priority = priority;
        t.input = input;
        t.output = output;
        t.errorMessage = errorMessage;
        t.retryCount = retryCount;
        t.maxRetries = maxRetries;
        t.startedAt = startedAt;
        t.completedAt = completedAt;
        t.scheduledAt = scheduledAt;
        t.createdAt = createdAt;
        t.updatedAt = updatedAt;
        return t;
    }

    public void start() {
        this.status = TaskStatus.RUNNING;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void complete(Map<String, Object> output) {
        this.status = TaskStatus.COMPLETED;
        this.output = output;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void fail(String error) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = error;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    public void incrementRetry() {
        this.retryCount++;
        this.status = TaskStatus.PENDING;
        this.updatedAt = Instant.now();
    }

    public void markRunning() {
        this.status = TaskStatus.RUNNING;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public AgentTaskId taskId() { return taskId; }
    public UUID missionId() { return missionId; }
    public UUID userId() { return userId; }
    public TaskType taskType() { return taskType; }
    public TaskStatus status() { return status; }
    public int priority() { return priority; }
    public String description() { return description; }
    public Map<String, Object> input() { return input; }
    public Map<String, Object> output() { return output; }
    public String errorMessage() { return errorMessage; }
    public int retryCount() { return retryCount; }
    public int maxRetries() { return maxRetries; }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }
    public Instant scheduledAt() { return scheduledAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
