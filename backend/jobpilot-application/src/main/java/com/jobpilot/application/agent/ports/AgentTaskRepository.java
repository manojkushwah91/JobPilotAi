package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.AgentTask;
import com.jobpilot.domain.agent.AgentTaskId;
import com.jobpilot.domain.agent.TaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentTaskRepository {

    AgentTask save(AgentTask task);

    Optional<AgentTask> findById(AgentTaskId taskId);

    List<AgentTask> findByMissionId(UUID missionId);

    List<AgentTask> findByStatus(TaskStatus status);

    List<AgentTask> findPendingTasks(int limit);

    List<AgentTask> findFailedTasksThatCanRetry(int limit);

    void delete(AgentTaskId taskId);
}
