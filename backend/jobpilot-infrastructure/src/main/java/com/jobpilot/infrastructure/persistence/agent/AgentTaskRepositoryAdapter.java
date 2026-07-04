package com.jobpilot.infrastructure.persistence.agent;

import com.jobpilot.application.agent.ports.AgentTaskRepository;
import com.jobpilot.domain.agent.AgentTask;
import com.jobpilot.domain.agent.AgentTaskId;
import com.jobpilot.domain.agent.TaskStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgentTaskRepositoryAdapter implements AgentTaskRepository {

    private final AgentTaskJpaRepository jpaRepository;

    public AgentTaskRepositoryAdapter(AgentTaskJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AgentTask save(AgentTask task) {
        var entity = AgentTaskJpaEntity.fromDomain(task);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AgentTask> findById(AgentTaskId taskId) {
        return jpaRepository.findById(taskId.value()).map(AgentTaskJpaEntity::toDomain);
    }

    @Override
    public List<AgentTask> findByMissionId(UUID missionId) {
        return jpaRepository.findByMissionId(missionId).stream()
            .map(AgentTaskJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<AgentTask> findByStatus(TaskStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(AgentTaskJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<AgentTask> findPendingTasks(int limit) {
        return jpaRepository.findPendingTasks(PageRequest.of(0, limit)).stream()
            .map(AgentTaskJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<AgentTask> findFailedTasksThatCanRetry(int limit) {
        return jpaRepository.findFailedTasksThatCanRetry(PageRequest.of(0, limit)).stream()
            .map(AgentTaskJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void delete(AgentTaskId taskId) {
        jpaRepository.deleteById(taskId.value());
    }
}
