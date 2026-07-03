package com.jobpilot.infrastructure.persistence.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {
    Optional<EmailVerificationTokenEntity> findByTokenAndUsedFalse(String token);
    void deleteByUserId(UUID userId);
}
