package com.jobpilot.domain.identity;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static final String VALID_HASH = "$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123";

    @Test
    void shouldRegisterNewUser() {
        var email = Email.from("test@example.com");
        var passwordHash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, "Test User", passwordHash);

        assertThat(user.userId()).isNotNull();
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.passwordHash()).isEqualTo(passwordHash);
        assertThat(user.role()).isEqualTo(Role.FREE);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    void shouldRegisterDomainEvent() {
        var email = Email.from("test@example.com");
        var passwordHash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, "Test User", passwordHash);

        var events = user.drainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).eventType()).isEqualTo("user.registered");
    }

    @Test
    void shouldVerifyEmail() {
        var user = createTestUser();
        user.verifyEmail();

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.emailVerifiedAt()).isNotNull();

        var events = user.drainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).eventType()).isEqualTo("user.verified");
    }

    @Test
    void shouldNotReVerifyEmail() {
        var user = createTestUser();
        user.verifyEmail();
        user.drainEvents();
        user.verifyEmail();

        assertThat(user.drainEvents()).isEmpty();
    }

    @Test
    void shouldUpdateRole() {
        var user = createTestUser();
        user.updateRole(Role.PRO);
        assertThat(user.role()).isEqualTo(Role.PRO);
    }

    @Test
    void shouldSoftDelete() {
        var user = createTestUser();
        user.softDelete();

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.deletedAt()).isNotNull();

        var events = user.drainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).eventType()).isEqualTo("user.deleted");
    }

    @Test
    void shouldNotDoubleDelete() {
        var user = createTestUser();
        user.softDelete();
        user.drainEvents();
        user.softDelete();

        assertThat(user.drainEvents()).isEmpty();
    }

    @Test
    void shouldReconstituteFromPersistence() {
        var id = UUID.randomUUID();
        var userId = UserId.from(id);
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");

        var user = User.reconstitute(id, userId, "Test User", email, hash, Role.PRO, true, null,
            java.util.Set.of(), false, null, java.time.Instant.now(), java.time.Instant.now());

        assertThat(user.userId()).isEqualTo(userId);
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.role()).isEqualTo(Role.PRO);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.drainEvents()).isEmpty();
    }

    private static User createTestUser() {
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, "Test User", hash);
        user.drainEvents();
        return user;
    }
}
