package com.jobpilot.infrastructure.persistence.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagJpaRepository extends JpaRepository<FeatureFlagEntity, String> {}
