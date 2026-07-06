package com.jobpilot.infrastructure.ratelimiter;

import com.jobpilot.application.ratelimiter.RateLimiterPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev | test")
public class InMemoryRateLimiterAdapter implements RateLimiterPort {

    private final ConcurrentHashMap<String, Long> lastAccess = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int capacity, int refillTokens, int refillDurationSeconds) {
        var now = System.currentTimeMillis() / 1000;
        var last = lastAccess.getOrDefault(key, 0L);
        if (now - last >= refillDurationSeconds) {
            lastAccess.put(key, now);
            return true;
        }
        return false;
    }
}
