package com.jobpilot.infrastructure.auth;

import com.jobpilot.application.identity.ports.RevokedTokenStore;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@Profile("!dev & !test")
@Primary
public class RedisRevokedTokenStore implements RevokedTokenStore {

    private static final String PREFIX = "revoked:token:";
    private static final long DEFAULT_EXPIRATION = 86400;

    private final StringRedisTemplate redis;

    public RedisRevokedTokenStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void revoke(String tokenId) {
        redis.opsForValue().set(PREFIX + tokenId, "revoked", DEFAULT_EXPIRATION, TimeUnit.SECONDS);
    }

    @Override
    public boolean isRevoked(String tokenId) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + tokenId));
    }
}
