package com.jobpilot.common.exception;

public class RateLimitException extends BaseException {
    private final long retryAfterSeconds;

    public RateLimitException(long retryAfterSeconds) {
        super("RATE_LIMIT_EXCEEDED", "Too many requests. Retry after " + retryAfterSeconds + "s");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() { return retryAfterSeconds; }

    @Override
    public int httpStatus() { return 429; }
}
