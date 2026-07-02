package com.jobpilot.common.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(String resourceType, Object id) {
        super("RESOURCE_NOT_FOUND", resourceType + " not found: " + id, resourceType, id);
    }

    @Override
    public int httpStatus() { return 404; }
}
