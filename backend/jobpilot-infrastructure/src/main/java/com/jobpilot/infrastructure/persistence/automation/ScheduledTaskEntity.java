package com.jobpilot.infrastructure.persistence.automation;

import com.jobpilot.domain.automation.ScheduledTask;
import com.jobpilot.domain.automation.ScheduledTaskId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scheduled_tasks")
public class ScheduledTaskEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "task_type", nullable = false) private String taskType;
    @Column(name = "payload", columnDefinition = "jsonb") private String payload;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "scheduled_at", nullable = false) private Instant scheduledAt;
    @Column(name = "executed_at") private Instant executedAt;

    protected ScheduledTaskEntity() {}

    public static ScheduledTaskEntity fromDomain(ScheduledTask t) {
        var e = new ScheduledTaskEntity();
        e.id = t.taskId().value();
        e.userId = t.userId();
        e.taskType = t.taskType();
        e.payload = t.payload();
        e.status = t.status().name();
        e.scheduledAt = t.scheduledAt();
        e.executedAt = t.executedAt();
        return e;
    }

    public ScheduledTask toDomain() {
        return ScheduledTask.reconstitute(
            ScheduledTaskId.from(id), userId, taskType, payload,
            ScheduledTask.Status.valueOf(status), scheduledAt, executedAt,
            createdAt, updatedAt
        );
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTaskType() { return taskType; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getExecutedAt() { return executedAt; }
}
