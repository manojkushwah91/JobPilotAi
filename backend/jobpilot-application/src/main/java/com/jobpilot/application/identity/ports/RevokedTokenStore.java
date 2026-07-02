package com.jobpilot.application.identity.ports;

public interface RevokedTokenStore {
    void revoke(String token);
    boolean isRevoked(String token);
}
