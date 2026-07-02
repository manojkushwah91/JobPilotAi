package com.jobpilot.infrastructure.security;

import com.jobpilot.application.identity.ports.RevokedTokenStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class InMemoryRevokedTokenStore implements RevokedTokenStore {

    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    @Override
    public void revoke(String token) {
        revokedTokens.add(token);
    }

    @Override
    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
}
