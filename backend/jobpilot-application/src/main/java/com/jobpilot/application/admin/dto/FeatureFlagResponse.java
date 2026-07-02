package com.jobpilot.application.admin.dto;

import com.jobpilot.domain.admin.FeatureFlag;
import java.time.Instant;

public record FeatureFlagResponse(String key, boolean enabled, String description, Instant updatedAt) {
    public static FeatureFlagResponse from(FeatureFlag flag, Instant updatedAt) {
        return new FeatureFlagResponse(flag.getKey(), flag.isEnabled(), flag.getDescription(), updatedAt);
    }
}
