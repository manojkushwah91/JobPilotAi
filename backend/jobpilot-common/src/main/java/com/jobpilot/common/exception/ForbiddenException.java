package com.jobpilot.common.exception;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }

    @Override
    public int httpStatus() { return 403; }
}
