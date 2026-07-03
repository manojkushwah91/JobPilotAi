package com.jobpilot.infrastructure.persistence.identity;

import com.jobpilot.application.identity.ports.PasswordResetTokenRepository;
import com.jobpilot.domain.identity.PasswordResetToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryImpl(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        var entity = PasswordResetTokenEntity.fromDomain(token);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByTokenAndUsedFalse(token)
            .map(PasswordResetTokenEntity::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
