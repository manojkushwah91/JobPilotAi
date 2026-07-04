package com.jobpilot.infrastructure.persistence.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavedJobJpaRepository extends JpaRepository<SavedJobEntity, SavedJobId> {
    List<SavedJobEntity> findByUserId(UUID userId);
    void deleteByUserIdAndJobListingId(UUID userId, UUID jobListingId);
    boolean existsByUserIdAndJobListingId(UUID userId, UUID jobListingId);
}
