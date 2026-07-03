package com.jobpilot.infrastructure.persistence.identity;

import com.jobpilot.domain.identity.PasswordResetToken;
import com.jobpilot.domain.identity.PasswordResetTokenId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity extends BaseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "used_at")
    private Instant usedAt;

    protected PasswordResetTokenEntity() {}

    public static PasswordResetTokenEntity fromDomain(PasswordResetToken domain) {
        var entity = new PasswordResetTokenEntity();
        entity.id = domain.id().value();
        entity.userId = domain.userId();
        entity.token = domain.token();
        entity.expiresAt = domain.expiresAt();
        entity.used = domain.isUsed();
        entity.usedAt = domain.usedAt();
        return entity;
    }

    public PasswordResetToken toDomain() {
        return PasswordResetToken.reconstitute(
            PasswordResetTokenId.from(id),
            userId,
            token,
            expiresAt,
            used,
            usedAt,
            createdAt
        );
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public Instant getUsedAt() { return usedAt; }
}
