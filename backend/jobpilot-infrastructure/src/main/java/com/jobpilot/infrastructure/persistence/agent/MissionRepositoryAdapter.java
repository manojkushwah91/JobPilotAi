package com.jobpilot.infrastructure.persistence.agent;

import com.jobpilot.application.agent.ports.MissionRepository;
import com.jobpilot.domain.agent.Mission;
import com.jobpilot.domain.agent.MissionId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MissionRepositoryAdapter implements MissionRepository {

    private final MissionJpaRepository jpaRepository;

    public MissionRepositoryAdapter(MissionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Mission save(Mission mission) {
        var entity = MissionJpaEntity.fromDomain(mission);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Mission> findById(MissionId missionId) {
        return jpaRepository.findById(missionId.value()).map(MissionJpaEntity::toDomain);
    }

    @Override
    public List<Mission> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
            .map(MissionJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Mission> findByStatus(com.jobpilot.domain.agent.MissionStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(MissionJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void delete(MissionId missionId) {
        jpaRepository.deleteById(missionId.value());
    }
}
