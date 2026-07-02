package com.jobpilot.application.ratelimiter;

public interface RateLimiterPort {
    boolean tryAcquire(String key, int capacity, int refillTokens, int refillDurationSeconds);
}
