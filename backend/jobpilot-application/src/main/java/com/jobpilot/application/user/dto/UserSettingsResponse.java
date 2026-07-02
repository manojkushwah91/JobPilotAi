package com.jobpilot.application.user.dto;

import java.util.Map;

public record UserSettingsResponse(
    Map<String, Object> jobPreferences,
    Map<String, Object> notificationPrefs,
    Map<String, Object> privacySettings,
    Map<String, Object> aiPreferences,
    Map<String, Object> appearance
) {}
