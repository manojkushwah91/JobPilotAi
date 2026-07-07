package com.jobpilot.infrastructure.ratelimiter;

import com.jobpilot.application.ratelimiter.RateLimiterPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile({"dev", "test"})
public class InMemoryRateLimiterAdapter implements RateLimiterPort {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int capacity, int refillTokens, int refillDurationSeconds) {
        var bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillTokens, refillDurationSeconds));
        return bucket.tryAcquire();
    }

    private static class TokenBucket {
        private final int capacity;
        private final int refillTokens;
        private final int refillDurationSeconds;
        private final AtomicInteger tokens;
        private volatile long lastRefillTime;

        TokenBucket(int capacity, int refillTokens, int refillDurationSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillDurationSeconds = refillDurationSeconds;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = System.currentTimeMillis() / 1000;
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            var now = System.currentTimeMillis() / 1000;
            var elapsed = now - lastRefillTime;
            if (elapsed >= refillDurationSeconds) {
                tokens.set(capacity);
                lastRefillTime = now;
            }
        }
    }
}
