package com.jobpilot.common.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BaseExceptionTest {

    @Test
    void shouldCreateNotFoundException() {
        var ex = new NotFoundException("User", "123");
        assertThat(ex.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(ex.httpStatus()).isEqualTo(404);
        assertThat(ex.getMessage()).contains("User").contains("123");
    }

    @Test
    void shouldCreateValidationException() {
        var ex = new ValidationException("email", "must not be blank");
        assertThat(ex.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(ex.httpStatus()).isEqualTo(400);
        assertThat(ex.getErrors()).hasSize(1);
    }

    @Test
    void shouldCreateDuplicateException() {
        var ex = new DuplicateException("User", "email", "test@test.com");
        assertThat(ex.getCode()).isEqualTo("CONFLICT");
        assertThat(ex.httpStatus()).isEqualTo(409);
    }

    @Test
    void shouldCreateUnauthorizedException() {
        var ex = new UnauthorizedException("Invalid credentials");
        assertThat(ex.getCode()).isEqualTo("UNAUTHORIZED");
        assertThat(ex.httpStatus()).isEqualTo(401);
    }

    @Test
    void shouldCreateForbiddenException() {
        var ex = new ForbiddenException("Insufficient privileges");
        assertThat(ex.getCode()).isEqualTo("FORBIDDEN");
        assertThat(ex.httpStatus()).isEqualTo(403);
    }

    @Test
    void shouldCreateRateLimitException() {
        var ex = new RateLimitException(30);
        assertThat(ex.getCode()).isEqualTo("RATE_LIMIT_EXCEEDED");
        assertThat(ex.httpStatus()).isEqualTo(429);
        assertThat(ex.getRetryAfterSeconds()).isEqualTo(30);
    }
}
