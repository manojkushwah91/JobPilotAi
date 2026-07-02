package com.jobpilot.application.user.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.Map;
import java.util.UUID;

public record UpdateSettingsCommand(
    UUID userId,
    Map<String, Object> jobPreferences,
    Map<String, Object> notificationPrefs,
    Map<String, Object> privacySettings,
    Map<String, Object> aiPreferences,
    Map<String, Object> appearance
) {
    public UpdateSettingsCommand {
        if (userId == null) {
            throw new ValidationException("userId", "User ID must not be null");
        }
        if (jobPreferences == null) jobPreferences = Map.of();
        if (notificationPrefs == null) notificationPrefs = Map.of();
        if (privacySettings == null) privacySettings = Map.of();
        if (aiPreferences == null) aiPreferences = Map.of();
        if (appearance == null) appearance = Map.of();
    }
}
