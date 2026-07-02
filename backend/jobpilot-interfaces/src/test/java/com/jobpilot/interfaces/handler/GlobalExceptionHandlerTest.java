package com.jobpilot.interfaces.handler;

import com.jobpilot.common.exception.*;
import com.jobpilot.common.model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleNotFoundException() {
        var ex = new NotFoundException("User", "123");
        var response = handler.handleBase(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void shouldHandleValidationException() {
        var ex = new ValidationException("email", "must not be blank");
        var response = handler.handleBase(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void shouldHandleUnauthorizedException() {
        var ex = new UnauthorizedException("Invalid token");
        var response = handler.handleBase(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void shouldHandleForbiddenException() {
        var ex = new ForbiddenException("Access denied");
        var response = handler.handleBase(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void shouldHandleDuplicateException() {
        var ex = new DuplicateException("User", "email", "test@test.com");
        var response = handler.handleBase(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void shouldHandleRateLimitException() {
        var ex = new RateLimitException(30);
        var response = handler.handleBase(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(429);
    }

    @Test
    void shouldHandleGenericException() {
        var ex = new RuntimeException("Unexpected");
        var response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
    }
}
