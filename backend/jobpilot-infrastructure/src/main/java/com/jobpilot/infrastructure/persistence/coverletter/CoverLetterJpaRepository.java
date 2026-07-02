package com.jobpilot.infrastructure.persistence.coverletter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CoverLetterJpaRepository extends JpaRepository<CoverLetterEntity, UUID> {
    List<CoverLetterEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
