package com.jobpilot.application.user.service;

import com.jobpilot.application.user.dto.UpdateSettingsCommand;
import com.jobpilot.application.user.ports.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateUserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UpdateUserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    public void execute(UpdateSettingsCommand command) {
        userSettingsRepository.save(
            command.userId(),
            command.jobPreferences(),
            command.notificationPrefs(),
            command.privacySettings(),
            command.aiPreferences(),
            command.appearance()
        );
    }
}
