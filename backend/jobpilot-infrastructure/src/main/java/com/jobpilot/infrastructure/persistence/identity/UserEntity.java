package com.jobpilot.infrastructure.persistence.identity;

import com.jobpilot.domain.identity.*;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity extends BaseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "tier")
    private String tier;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "locale")
    private String locale;

    protected UserEntity() {}

    public static UserEntity fromDomain(User user) {
        var entity = new UserEntity();
        entity.id = user.userId().value();
        entity.email = user.email().value();
        entity.passwordHash = user.passwordHash().value();
        entity.name = user.email().value().split("@")[0];
        entity.role = user.role();
        entity.tier = user.role().name();
        entity.emailVerified = user.isEmailVerified();
        entity.emailVerifiedAt = user.emailVerifiedAt();
        entity.deletedAt = user.deletedAt();
        return entity;
    }

    public User toDomain() {
        var userId = UserId.from(id);
        var emailVo = Email.from(email);
        var passwordHashVo = PasswordHash.from(passwordHash);

        return User.reconstitute(
            id, userId, emailVo, passwordHashVo, role,
            emailVerified, emailVerifiedAt,
            new HashSet<>(), deletedAt != null, deletedAt,
            createdAt, updatedAt
        );
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public String getTier() { return tier; }
    public boolean isEmailVerified() { return emailVerified; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public String getLocale() { return locale; }
}
