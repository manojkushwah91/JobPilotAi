package com.jobpilot.infrastructure.persistence.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentTaskJpaRepository extends JpaRepository<AgentTaskJpaEntity, UUID> {

    List<AgentTaskJpaEntity> findByMissionId(UUID missionId);

    List<AgentTaskJpaEntity> findByStatus(com.jobpilot.domain.agent.TaskStatus status);

    @Query("SELECT t FROM AgentTaskJpaEntity t WHERE t.status = 'PENDING' ORDER BY t.priority DESC, t.createdAt ASC")
    List<AgentTaskJpaEntity> findPendingTasks(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM AgentTaskJpaEntity t WHERE t.status = 'FAILED' AND t.retryCount < t.maxRetries")
    List<AgentTaskJpaEntity> findFailedTasksThatCanRetry(org.springframework.data.domain.Pageable pageable);
}
