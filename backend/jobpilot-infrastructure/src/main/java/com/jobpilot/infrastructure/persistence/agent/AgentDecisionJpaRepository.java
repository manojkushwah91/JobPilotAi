package com.jobpilot.infrastructure.persistence.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentDecisionJpaRepository extends JpaRepository<AgentDecisionJpaEntity, UUID> {

    List<AgentDecisionJpaEntity> findByMissionId(UUID missionId);

    List<AgentDecisionJpaEntity> findByMissionIdAndExecuted(UUID missionId, boolean executed);
}
