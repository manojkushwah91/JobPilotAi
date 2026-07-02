package com.jobpilot.domain.identity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordHashTest {

    private static final String VALID_HASH = "$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123";

    @Test
    void shouldCreateValidBcryptHash() {
        var hash = PasswordHash.from(VALID_HASH);
        assertThat(hash.value()).isNotNull();
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> PasswordHash.from(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlank() {
        assertThatThrownBy(() -> PasswordHash.from("  "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidFormat() {
        assertThatThrownBy(() -> PasswordHash.from("not-a-bcrypt-hash"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualForSameHash() {
        assertThat(PasswordHash.from(VALID_HASH)).isEqualTo(PasswordHash.from(VALID_HASH));
    }
}
