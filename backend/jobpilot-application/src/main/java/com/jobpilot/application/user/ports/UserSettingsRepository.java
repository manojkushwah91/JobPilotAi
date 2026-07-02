package com.jobpilot.application.user.ports;

import com.jobpilot.application.user.dto.UserSettingsResponse;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserSettingsRepository {
    Optional<UserSettingsResponse> findByUserId(UUID userId);
    void save(UUID userId, Map<String, Object> jobPrefs, Map<String, Object> notifPrefs,
              Map<String, Object> privacy, Map<String, Object> aiPrefs, Map<String, Object> appearance);
}
