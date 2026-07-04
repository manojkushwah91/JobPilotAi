package com.jobpilot.infrastructure.persistence.agent;

import com.jobpilot.application.agent.ports.ObservationRepository;
import com.jobpilot.domain.agent.AgentObservation;
import com.jobpilot.domain.agent.ObservationId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ObservationRepositoryAdapter implements ObservationRepository {

    private final AgentObservationJpaRepository jpaRepository;

    public ObservationRepositoryAdapter(AgentObservationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AgentObservation save(AgentObservation observation) {
        var entity = AgentObservationJpaEntity.fromDomain(observation);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AgentObservation> findById(ObservationId observationId) {
        return jpaRepository.findById(observationId.value()).map(AgentObservationJpaEntity::toDomain);
    }

    @Override
    public List<AgentObservation> findByMissionId(UUID missionId) {
        return jpaRepository.findByMissionId(missionId).stream()
            .map(AgentObservationJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<AgentObservation> findRecentByMissionId(UUID missionId, int limit) {
        return jpaRepository.findTop10ByMissionIdOrderByCreatedAtDesc(missionId).stream()
            .limit(limit)
            .map(AgentObservationJpaEntity::toDomain)
            .toList();
    }
}
