package com.jobpilot.common.model;

public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    Pagination pagination,
    ErrorDetail error
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, Pagination pagination) {
        return new ApiResponse<>(true, "Success", data, pagination, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "Created", data, null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, message, null, null, new ErrorDetail(code, message, null));
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return new ApiResponse<>(false, message, null, null, new ErrorDetail(code, message, details));
    }

    public record Pagination(int page, int size, long totalElements, int totalPages) {
        public static Pagination of(int page, int size, long totalElements) {
            var totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
            return new Pagination(page, size, totalElements, totalPages);
        }
    }

    public record ErrorDetail(String code, String message, Object details) {}
}
