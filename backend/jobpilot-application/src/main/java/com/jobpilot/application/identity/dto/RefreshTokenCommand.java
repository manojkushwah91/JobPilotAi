package com.jobpilot.application.identity.dto;

import com.jobpilot.common.exception.ValidationException;

public record RefreshTokenCommand(
    String refreshToken
) {

    public RefreshTokenCommand {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("refreshToken", "Refresh token must not be blank");
        }
    }
}
