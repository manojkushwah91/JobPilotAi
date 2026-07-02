package com.jobpilot.infrastructure.persistence.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiUsageLogJpaRepository extends JpaRepository<AiUsageLogEntity, UUID> {
}
