package com.jobpilot.application.identity.ports;

import java.time.Instant;
import java.util.List;

public interface TokenProvider {
    String generateAccessToken(String userId, String email, List<String> roles, String tier);
    String generateRefreshToken(String userId, String family);
    boolean validateToken(String token);
    String getUserIdFromToken(String token);
    List<String> getRolesFromToken(String token);
    String getTokenType(String token);
    Instant getExpirationFromToken(String token);
}
