package com.jobpilot.application.identity.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String tokenType,
    UserResponse user
) {

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }

    public record UserResponse(
        String id,
        String email,
        String role,
        boolean emailVerified
    ) {}
}
