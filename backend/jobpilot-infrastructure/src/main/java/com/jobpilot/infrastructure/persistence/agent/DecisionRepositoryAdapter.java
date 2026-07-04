package com.jobpilot.infrastructure.persistence.agent;

import com.jobpilot.application.agent.ports.DecisionRepository;
import com.jobpilot.domain.agent.AgentDecision;
import com.jobpilot.domain.agent.DecisionId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DecisionRepositoryAdapter implements DecisionRepository {

    private final AgentDecisionJpaRepository jpaRepository;

    public DecisionRepositoryAdapter(AgentDecisionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AgentDecision save(AgentDecision decision) {
        var entity = AgentDecisionJpaEntity.fromDomain(decision);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AgentDecision> findById(DecisionId decisionId) {
        return jpaRepository.findById(decisionId.value()).map(AgentDecisionJpaEntity::toDomain);
    }

    @Override
    public List<AgentDecision> findByMissionId(UUID missionId) {
        return jpaRepository.findByMissionId(missionId).stream()
            .map(AgentDecisionJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<AgentDecision> findUnexecutedByMissionId(UUID missionId) {
        return jpaRepository.findByMissionIdAndExecuted(missionId, false).stream()
            .map(AgentDecisionJpaEntity::toDomain)
            .toList();
    }
}
