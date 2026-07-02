package com.jobpilot.application.admin.dto;

import com.jobpilot.common.exception.ValidationException;

public record UpdateFeatureFlagCommand(String key, boolean enabled) {
    public UpdateFeatureFlagCommand {
        if (key == null || key.isBlank()) throw new ValidationException("key", "Key must not be blank");
    }
}
