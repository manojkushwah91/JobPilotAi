package com.jobpilot.common.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    @Override
    public int httpStatus() { return 401; }
}
