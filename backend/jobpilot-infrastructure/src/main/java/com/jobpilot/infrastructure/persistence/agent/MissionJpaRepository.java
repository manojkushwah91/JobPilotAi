package com.jobpilot.infrastructure.persistence.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionJpaRepository extends JpaRepository<MissionJpaEntity, UUID> {

    List<MissionJpaEntity> findByUserId(UUID userId);

    List<MissionJpaEntity> findByStatus(com.jobpilot.domain.agent.MissionStatus status);

    @Query("SELECT m FROM MissionJpaEntity m WHERE m.status = 'ACTIVE' AND m.deadlineAt > CURRENT_TIMESTAMP")
    List<MissionJpaEntity> findActiveMissions();
}
