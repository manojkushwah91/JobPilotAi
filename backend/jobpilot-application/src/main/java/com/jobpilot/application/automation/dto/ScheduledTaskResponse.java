package com.jobpilot.application.automation.dto;

import com.jobpilot.domain.automation.ScheduledTask;
import java.time.Instant;
import java.util.UUID;

public record ScheduledTaskResponse(
    String taskId, UUID userId, String taskType, String payload, String status,
    Instant scheduledAt, Instant executedAt, Instant createdAt
) {
    public static ScheduledTaskResponse from(ScheduledTask t) {
        return new ScheduledTaskResponse(
            t.taskId().value().toString(), t.userId(), t.taskType(), t.payload(),
            t.status().name(), t.scheduledAt(), t.executedAt(), t.createdAt()
        );
    }
}
