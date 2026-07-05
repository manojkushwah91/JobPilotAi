package com.jobpilot.infrastructure.persistence.automation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationResultJpaRepository extends JpaRepository<ApplicationResultJpaEntity, String> {

    List<ApplicationResultJpaEntity> findBySessionId(String sessionId);

    List<ApplicationResultJpaEntity> findByOutcome(String outcome);

    List<ApplicationResultJpaEntity> findByMissionId(String missionId);

    List<ApplicationResultJpaEntity> findByUserId(String userId);

    @Query("SELECT COUNT(r) FROM ApplicationResultJpaEntity r WHERE r.outcome = 'SUBMITTED'")
    int countSubmittedApplications();

    @Query("SELECT COUNT(r) FROM ApplicationResultJpaEntity r WHERE r.outcome = 'FAILED'")
    int countFailedApplications();

    @Query("SELECT COUNT(r) FROM ApplicationResultJpaEntity r WHERE r.missionId = :missionId AND r.outcome = 'SUBMITTED'")
    int countSubmittedByMission(@Param("missionId") String missionId);

    boolean existsByJobUrlAndOutcomeIn(String jobUrl, List<String> outcomes);

    List<ApplicationResultJpaEntity> findTop50ByOrderByCreatedAtDesc();
}
