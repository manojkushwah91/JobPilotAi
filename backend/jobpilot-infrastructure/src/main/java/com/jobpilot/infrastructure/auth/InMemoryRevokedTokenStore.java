package com.jobpilot.infrastructure.auth;

import com.jobpilot.application.identity.ports.RevokedTokenStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev | test")
public class InMemoryRevokedTokenStore implements RevokedTokenStore {

    private final Set<String> revokedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void revoke(String tokenId) {
        revokedTokens.add(tokenId);
    }

    @Override
    public boolean isRevoked(String tokenId) {
        return revokedTokens.contains(tokenId);
    }
}
