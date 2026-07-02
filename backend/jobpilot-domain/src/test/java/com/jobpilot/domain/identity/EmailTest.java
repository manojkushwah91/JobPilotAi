package com.jobpilot.domain.identity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        var email = Email.from("Test@Example.com");
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> Email.from(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() -> Email.from("  "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> Email.from("not-an-email"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmailWithoutDomain() {
        assertThatThrownBy(() -> Email.from("user@"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualForSameEmail() {
        var e1 = Email.from("test@example.com");
        var e2 = Email.from("TEST@example.com");
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentEmails() {
        var e1 = Email.from("a@example.com");
        var e2 = Email.from("b@example.com");
        assertThat(e1).isNotEqualTo(e2);
    }
}
