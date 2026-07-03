package com.jobpilot.infrastructure.persistence.identity;

import com.jobpilot.application.identity.ports.EmailVerificationTokenRepository;
import com.jobpilot.domain.identity.EmailVerificationToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class EmailVerificationTokenRepositoryImpl implements EmailVerificationTokenRepository {

    private final EmailVerificationTokenJpaRepository jpaRepository;

    public EmailVerificationTokenRepositoryImpl(EmailVerificationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        var entity = EmailVerificationTokenEntity.fromDomain(token);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return jpaRepository.findByTokenAndUsedFalse(token)
            .map(EmailVerificationTokenEntity::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
