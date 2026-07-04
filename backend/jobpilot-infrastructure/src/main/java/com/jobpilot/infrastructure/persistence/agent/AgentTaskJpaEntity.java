package com.jobpilot.infrastructure.persistence.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_tasks")
public class AgentTaskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mission_id", nullable = false)
    private UUID missionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private com.jobpilot.domain.agent.TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.jobpilot.domain.agent.TaskStatus status;

    private int priority;

    private String description;

    @Column(columnDefinition = "jsonb")
    private String input;

    @Column(columnDefinition = "jsonb")
    private String output;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "max_retries")
    private int maxRetries;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AgentTaskJpaEntity() {}

    public static AgentTaskJpaEntity fromDomain(com.jobpilot.domain.agent.AgentTask task) {
        var entity = new AgentTaskJpaEntity();
        entity.id = task.taskId().value();
        entity.missionId = task.missionId();
        entity.userId = task.userId();
        entity.taskType = task.taskType();
        entity.status = task.status();
        entity.priority = task.priority();
        entity.description = task.description();
        entity.errorMessage = task.errorMessage();
        entity.retryCount = task.retryCount();
        entity.maxRetries = task.maxRetries();
        entity.startedAt = task.startedAt();
        entity.completedAt = task.completedAt();
        entity.scheduledAt = task.scheduledAt();
        entity.createdAt = task.createdAt();
        entity.updatedAt = task.updatedAt();
        return entity;
    }

    public com.jobpilot.domain.agent.AgentTask toDomain() {
        return com.jobpilot.domain.agent.AgentTask.reconstitute(
            com.jobpilot.domain.agent.AgentTaskId.from(id),
            missionId, userId,
            com.jobpilot.domain.agent.TaskType.valueOf(taskType.name()),
            com.jobpilot.domain.agent.TaskStatus.valueOf(status.name()),
            priority, description,
            null, null, errorMessage,
            retryCount, maxRetries,
            startedAt, completedAt, scheduledAt,
            createdAt, updatedAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMissionId() { return missionId; }
    public void setMissionId(UUID missionId) { this.missionId = missionId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public com.jobpilot.domain.agent.TaskType getTaskType() { return taskType; }
    public void setTaskType(com.jobpilot.domain.agent.TaskType taskType) { this.taskType = taskType; }
    public com.jobpilot.domain.agent.TaskStatus getStatus() { return status; }
    public void setStatus(com.jobpilot.domain.agent.TaskStatus status) { this.status = status; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
