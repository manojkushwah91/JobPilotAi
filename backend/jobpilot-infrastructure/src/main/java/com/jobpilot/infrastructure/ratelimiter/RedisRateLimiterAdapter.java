package com.jobpilot.infrastructure.ratelimiter;

import com.jobpilot.application.ratelimiter.RateLimiterPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!dev")
@Primary
public class RedisRateLimiterAdapter implements RateLimiterPort {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiterAdapter.class);

    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refillTokens = tonumber(ARGV[2])
        local refillDuration = tonumber(ARGV[3])
        local now = tonumber(ARGV[4])

        local bucket = redis.call('hmget', key, 'tokens', 'lastRefill')
        local tokens = tonumber(bucket[1]) or capacity
        local lastRefill = tonumber(bucket[2]) or 0

        local elapsed = now - lastRefill
        if elapsed >= refillDuration then
            local cycles = math.floor(elapsed / refillDuration)
            tokens = math.min(capacity, tokens + cycles * refillTokens)
            lastRefill = now
        end

        if tokens >= 1 then
            tokens = tokens - 1
            redis.call('hmset', key, 'tokens', tokens, 'lastRefill', lastRefill)
            redis.call('expire', key, refillDuration * 2)
            return 1
        else
            return 0
        end
        """;

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> script;

    public RedisRateLimiterAdapter(StringRedisTemplate redis) {
        this.redis = redis;
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA_SCRIPT);
        this.script.setResultType(Long.class);
    }

    @Override
    public boolean tryAcquire(String key, int capacity, int refillTokens, int refillDurationSeconds) {
        var now = System.currentTimeMillis() / 1000;
        var result = redis.execute(script, List.of(key),
            String.valueOf(capacity),
            String.valueOf(refillTokens),
            String.valueOf(refillDurationSeconds),
            String.valueOf(now));
        return Long.valueOf(1).equals(result);
    }
}
