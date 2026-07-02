package com.jobpilot.common.exception;

public class DuplicateException extends BaseException {
    public DuplicateException(String resourceType, String field, Object value) {
        super("CONFLICT", resourceType + " with " + field + " '" + value + "' already exists",
            resourceType, field, value);
    }

    @Override
    public int httpStatus() { return 409; }
}
