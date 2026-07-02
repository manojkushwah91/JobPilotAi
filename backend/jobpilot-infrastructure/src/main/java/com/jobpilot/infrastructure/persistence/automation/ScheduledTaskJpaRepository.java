package com.jobpilot.infrastructure.persistence.automation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface ScheduledTaskJpaRepository extends JpaRepository<ScheduledTaskEntity, UUID> {
    Page<ScheduledTaskEntity> findByStatusAndScheduledAtBefore(String status, Instant before, Pageable pageable);
}
