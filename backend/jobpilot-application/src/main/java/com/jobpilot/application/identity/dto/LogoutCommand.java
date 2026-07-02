package com.jobpilot.application.identity.dto;

import com.jobpilot.common.exception.ValidationException;

public record LogoutCommand(
    String accessToken,
    String refreshToken
) {

    public LogoutCommand {
        if (accessToken == null && refreshToken == null) {
            throw new ValidationException("tokens", "At least one token must be provided");
        }
    }
}
