package com.jobpilot.interfaces.rest.v1.agent;

import com.jobpilot.domain.agent.AgentTask;
import com.jobpilot.domain.agent.TaskStatus;
import com.jobpilot.domain.agent.TaskType;

import java.time.Instant;

public record TaskResponse(
    String id,
    String missionId,
    String userId,
    TaskType taskType,
    TaskStatus status,
    int priority,
    String description,
    String errorMessage,
    int retryCount,
    int maxRetries,
    Instant startedAt,
    Instant completedAt,
    Instant createdAt
) {
    public static TaskResponse from(AgentTask task) {
        return new TaskResponse(
            task.taskId().value().toString(),
            task.missionId().toString(),
            task.userId().toString(),
            task.taskType(),
            task.status(),
            task.priority(),
            task.description(),
            task.errorMessage(),
            task.retryCount(),
            task.maxRetries(),
            task.startedAt(),
            task.completedAt(),
            task.createdAt()
        );
    }
}
