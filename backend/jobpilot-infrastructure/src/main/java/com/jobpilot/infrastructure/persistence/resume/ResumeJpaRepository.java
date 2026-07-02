package com.jobpilot.infrastructure.persistence.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeJpaRepository extends JpaRepository<ResumeEntity, UUID> {
    List<ResumeEntity> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    Optional<ResumeEntity> findByUserIdAndIsDefaultTrue(UUID userId);
}
