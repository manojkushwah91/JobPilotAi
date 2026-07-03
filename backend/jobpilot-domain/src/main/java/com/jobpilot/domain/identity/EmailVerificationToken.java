package com.jobpilot.domain.identity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class EmailVerificationToken {

    private final EmailVerificationTokenId id;
    private final UUID userId;
    private final String token;
    private final Instant expiresAt;
    private boolean used;
    private Instant usedAt;
    private final Instant createdAt;

    private EmailVerificationToken(EmailVerificationTokenId id, UUID userId, String token, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = false;
        this.usedAt = null;
        this.createdAt = Instant.now();
    }

    public static EmailVerificationToken create(UUID userId, String token, Duration validity) {
        var id = EmailVerificationTokenId.generate();
        var expiresAt = Instant.now().plus(validity);
        return new EmailVerificationToken(id, userId, token, expiresAt);
    }

    public static EmailVerificationToken reconstitute(EmailVerificationTokenId id, UUID userId, String token,
                                                       Instant expiresAt, boolean used, Instant usedAt,
                                                       Instant createdAt) {
        var evt = new EmailVerificationToken(id, userId, token, expiresAt);
        evt.used = used;
        evt.usedAt = usedAt;
        return evt;
    }

    public void markUsed() {
        this.used = true;
        this.usedAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public EmailVerificationTokenId id() { return id; }
    public UUID userId() { return userId; }
    public String token() { return token; }
    public Instant expiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public Instant usedAt() { return usedAt; }
    public Instant createdAt() { return createdAt; }
}
