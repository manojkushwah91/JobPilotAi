package com.jobpilot.common.exception;

import java.util.List;

public class ValidationException extends BaseException {
    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super("VALIDATION_ERROR", "Validation failed: " + errors.size() + " error(s)");
        this.errors = errors;
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", message);
        this.errors = List.of(new ValidationError(field, message));
    }

    public List<ValidationError> getErrors() { return errors; }

    @Override
    public int httpStatus() { return 400; }

    public record ValidationError(String field, String message) {}
}
