package com.jobpilot.infrastructure.persistence.interview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterviewSessionJpaRepository extends JpaRepository<InterviewSessionEntity, UUID> {
    Page<InterviewSessionEntity> findByUserId(UUID userId, Pageable pageable);
    Page<InterviewSessionEntity> findByCompanyId(UUID companyId, Pageable pageable);
}
