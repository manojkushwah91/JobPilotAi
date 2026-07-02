package com.jobpilot.application.admin.dto;

import com.jobpilot.common.exception.ValidationException;

public record FeatureFlagRequest(String key, boolean enabled, String description) {
    public FeatureFlagRequest {
        if (key == null || key.isBlank()) throw new ValidationException("key", "Key must not be blank");
    }
}
