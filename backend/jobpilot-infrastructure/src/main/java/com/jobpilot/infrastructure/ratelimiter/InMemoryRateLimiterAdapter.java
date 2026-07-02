package com.jobpilot.infrastructure.ratelimiter;

import com.jobpilot.application.ratelimiter.RateLimiterPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class InMemoryRateLimiterAdapter implements RateLimiterPort {

    private static final Logger log = LoggerFactory.getLogger(InMemoryRateLimiterAdapter.class);
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int capacity, int refillTokens, int refillDurationSeconds) {
        var bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillTokens, refillDurationSeconds));
        return bucket.tryAcquire();
    }

    private static class TokenBucket {
        private final int capacity;
        private final int refillTokens;
        private final long refillDurationMillis;
        private double tokens;
        private long lastRefillTimestamp;

        TokenBucket(int capacity, int refillTokens, int refillDurationSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillDurationMillis = refillDurationSeconds * 1000L;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens >= 1) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            var now = System.currentTimeMillis();
            var elapsed = now - lastRefillTimestamp;
            if (elapsed >= refillDurationMillis) {
                var cycles = elapsed / refillDurationMillis;
                tokens = Math.min(capacity, tokens + cycles * refillTokens);
                lastRefillTimestamp = now;
            }
        }
    }
}
