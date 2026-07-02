package com.jobpilot.application.user.service;

import com.jobpilot.application.user.dto.UserSettingsResponse;
import com.jobpilot.application.user.ports.UserSettingsRepository;
import com.jobpilot.common.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetUserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public GetUserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    public UserSettingsResponse execute(UUID userId) {
        return userSettingsRepository.findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("UserSettings", userId));
    }
}
