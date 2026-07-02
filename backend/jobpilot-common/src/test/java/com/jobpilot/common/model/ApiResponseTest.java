package com.jobpilot.common.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        var response = ApiResponse.ok("data");
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("data");
        assertThat(response.pagination()).isNull();
        assertThat(response.error()).isNull();
    }

    @Test
    void shouldCreateCreatedResponse() {
        var response = ApiResponse.created("new-resource");
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("Created");
    }

    @Test
    void shouldCreatePaginatedResponse() {
        var pagination = new ApiResponse.Pagination(0, 20, 100, 5);
        var response = ApiResponse.ok("data", pagination);
        assertThat(response.pagination()).isNotNull();
        assertThat(response.pagination().totalPages()).isEqualTo(5);
    }

    @Test
    void shouldCreateErrorResponse() {
        var response = ApiResponse.error("NOT_FOUND", "Resource not found");
        assertThat(response.success()).isFalse();
        assertThat(response.error().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    void shouldCreateErrorResponseWithDetails() {
        var response = ApiResponse.error("VALIDATION_ERROR", "Invalid input", java.util.Map.of("field", "must not be blank"));
        assertThat(response.error().details()).isNotNull();
    }

    @Test
    void shouldComputePagination() {
        var pagination = ApiResponse.Pagination.of(0, 20, 100);
        assertThat(pagination.page()).isZero();
        assertThat(pagination.size()).isEqualTo(20);
        assertThat(pagination.totalElements()).isEqualTo(100);
        assertThat(pagination.totalPages()).isEqualTo(5);
    }

    @Test
    void shouldRoundUpPagination() {
        var pagination = ApiResponse.Pagination.of(0, 20, 101);
        assertThat(pagination.totalPages()).isEqualTo(6);
    }

    @Test
    void shouldHandleZeroElements() {
        var pagination = ApiResponse.Pagination.of(0, 20, 0);
        assertThat(pagination.totalPages()).isZero();
    }
}
