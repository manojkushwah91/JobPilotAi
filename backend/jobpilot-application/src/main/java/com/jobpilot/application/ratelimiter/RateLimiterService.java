package com.jobpilot.application.ratelimiter;

import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final RateLimiterPort rateLimiterPort;

    public RateLimiterService(RateLimiterPort rateLimiterPort) {
        this.rateLimiterPort = rateLimiterPort;
    }

    public boolean tryAcquire(String key, int capacity, int refillTokens, int refillDurationSeconds) {
        return rateLimiterPort.tryAcquire(key, capacity, refillTokens, refillDurationSeconds);
    }
}
