package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseAggregateRoot;
import java.time.Instant;
import java.util.UUID;

public class ScheduledTask extends BaseAggregateRoot {

    public enum Status { PENDING, COMPLETED, FAILED }

    private ScheduledTaskId taskId;
    private UUID userId;
    private String taskType;
    private String payload;
    private Status status;
    private Instant scheduledAt;
    private Instant executedAt;
    private Instant createdAt;

    private ScheduledTask() {
        super();
    }

    private ScheduledTask(ScheduledTaskId taskId, UUID userId, String taskType, String payload, Instant scheduledAt) {
        super(taskId.value());
        this.taskId = taskId;
        this.userId = userId;
        this.taskType = taskType;
        this.payload = payload;
        this.status = Status.PENDING;
        this.scheduledAt = scheduledAt;
        this.createdAt = Instant.now();
    }

    public static ScheduledTask schedule(ScheduledTaskId taskId, UUID userId, String taskType, String payload, Instant scheduledAt) {
        return new ScheduledTask(taskId, userId, taskType, payload, scheduledAt);
    }

    public static ScheduledTask reconstitute(ScheduledTaskId taskId, UUID userId, String taskType, String payload,
            Status status, Instant scheduledAt, Instant executedAt, Instant createdAt, Instant updatedAt) {
        var t = new ScheduledTask();
        t.taskId = taskId;
        t.userId = userId;
        t.taskType = taskType;
        t.payload = payload;
        t.status = status;
        t.scheduledAt = scheduledAt;
        t.executedAt = executedAt;
        t.createdAt = createdAt;
        return t;
    }

    public void markCompleted() {
        this.status = Status.COMPLETED;
        this.executedAt = Instant.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.executedAt = Instant.now();
    }

    public ScheduledTaskId taskId() { return taskId; }
    public UUID userId() { return userId; }
    public String taskType() { return taskType; }
    public String payload() { return payload; }
    public Status status() { return status; }
    public Instant scheduledAt() { return scheduledAt; }
    public Instant executedAt() { return executedAt; }
    public Instant createdAt() { return createdAt; }
}
