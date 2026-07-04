package com.jobpilot.infrastructure.persistence.automation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationSessionJpaRepository extends JpaRepository<AutomationSessionEntity, UUID> {
    Page<AutomationSessionEntity> findByUserId(UUID userId, Pageable pageable);
    List<AutomationSessionEntity> findByStatus(String status, Pageable pageable);
}
