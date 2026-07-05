package com.jobpilot.infrastructure.persistence.automation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationResultJpaRepository extends JpaRepository<ApplicationResultJpaEntity, String> {

    List<ApplicationResultJpaEntity> findBySessionId(String sessionId);

    List<ApplicationResultJpaEntity> findByOutcome(String outcome);

    @Query("SELECT COUNT(r) FROM ApplicationResultJpaEntity r WHERE r.outcome = 'SUBMITTED'")
    int countSubmittedApplications();

    @Query("SELECT COUNT(r) FROM ApplicationResultJpaEntity r WHERE r.outcome = 'FAILED'")
    int countFailedApplications();
}
