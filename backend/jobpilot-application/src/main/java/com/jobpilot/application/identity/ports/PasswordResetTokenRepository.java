package com.jobpilot.application.identity.ports;

import com.jobpilot.domain.identity.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserId(UUID userId);
}
