package com.jobpilot.application.user.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.UUID;

public record UpdateProfileCommand(
    UUID userId,
    String name,
    String avatarUrl,
    String locale
) {
    public UpdateProfileCommand {
        if (userId == null) {
            throw new ValidationException("userId", "User ID must not be null");
        }
    }
}
