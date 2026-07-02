package com.jobpilot.domain.identity;

import com.jobpilot.domain.identity.events.UserDeletedEvent;
import com.jobpilot.domain.identity.events.UserRegisteredEvent;
import com.jobpilot.domain.identity.events.UserVerifiedEvent;
import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User extends BaseAggregateRoot {

    private final UserId userId;
    private Email email;
    private PasswordHash passwordHash;
    private Role role;
    private boolean emailVerified;
    private Instant emailVerifiedAt;
    private final Set<OAuthProvider> oauthProviders;
    private boolean deleted;
    private Instant deletedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UserId userId, Email email, PasswordHash passwordHash, Role role) {
        super(userId.value());
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = false;
        this.emailVerifiedAt = null;
        this.oauthProviders = new HashSet<>();
        this.deleted = false;
        this.deletedAt = null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static User register(Email email, PasswordHash passwordHash) {
        var userId = UserId.generate();
        var user = new User(userId, email, passwordHash, Role.FREE);
        user.registerEvent(new UserRegisteredEvent(userId, email, Role.FREE));
        return user;
    }

    public static User reconstitute(UUID id, UserId userId, Email email, PasswordHash passwordHash,
                                     Role role, boolean emailVerified, Instant emailVerifiedAt,
                                     Set<OAuthProvider> oauthProviders, boolean deleted,
                                     Instant deletedAt, Instant createdAt, Instant updatedAt) {
        var user = new User(userId, email, passwordHash, role);
        user.emailVerified = emailVerified;
        user.emailVerifiedAt = emailVerifiedAt;
        user.oauthProviders.addAll(oauthProviders);
        user.deleted = deleted;
        user.deletedAt = deletedAt;
        return user;
    }

    public void verifyEmail() {
        if (emailVerified) return;
        this.emailVerified = true;
        this.emailVerifiedAt = Instant.now();
        this.updatedAt = Instant.now();
        registerEvent(new UserVerifiedEvent(userId));
    }

    public void updateRole(Role newRole) {
        if (this.role == newRole) return;
        this.role = newRole;
        this.updatedAt = Instant.now();
    }

    public void updatePassword(PasswordHash newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (deleted) return;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
        registerEvent(new UserDeletedEvent(userId));
    }

    public void addOAuthProvider(OAuthProvider provider) {
        oauthProviders.add(provider);
        this.updatedAt = Instant.now();
    }

    public void removeOAuthProvider(OAuthProvider provider) {
        oauthProviders.remove(provider);
        this.updatedAt = Instant.now();
    }

    public UserId userId() { return userId; }
    public Email email() { return email; }
    public PasswordHash passwordHash() { return passwordHash; }
    public Role role() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public Instant emailVerifiedAt() { return emailVerifiedAt; }
    public Set<OAuthProvider> oauthProviders() { return Set.copyOf(oauthProviders); }
    public boolean isDeleted() { return deleted; }
    public Instant deletedAt() { return deletedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
