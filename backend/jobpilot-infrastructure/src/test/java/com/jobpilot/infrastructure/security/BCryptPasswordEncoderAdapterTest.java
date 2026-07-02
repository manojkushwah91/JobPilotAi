package com.jobpilot.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter adapter =
        new BCryptPasswordEncoderAdapter(new BCryptPasswordEncoder(4));

    @Test
    void shouldEncodeAndMatch() {
        var raw = "ValidPassword1!";
        var encoded = adapter.encode(raw);
        assertThat(encoded).startsWith("$2a$");
        assertThat(adapter.matches(raw, encoded)).isTrue();
    }

    @Test
    void shouldNotMatchWrongPassword() {
        var encoded = adapter.encode("ValidPassword1!");
        assertThat(adapter.matches("WrongPassword1!", encoded)).isFalse();
    }
}
