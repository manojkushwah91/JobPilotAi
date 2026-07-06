package com.jobpilot.infrastructure.persistence.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateProfileJpaRepository extends JpaRepository<CandidateProfileJpaEntity, UUID> {

    Optional<CandidateProfileJpaEntity> findByUserId(UUID userId);
}
