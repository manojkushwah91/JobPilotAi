package com.jobpilot.application.identity.ports;

import com.jobpilot.domain.identity.EmailVerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository {
    EmailVerificationToken save(EmailVerificationToken token);
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUserId(UUID userId);
}
