package com.jobpilot.application.automation.dto;

import com.jobpilot.common.exception.ValidationException;

public record StartAutomationCommand(String userId, String applicationId) {
    public StartAutomationCommand {
        if (userId == null || userId.isBlank()) throw new ValidationException("userId", "User ID must not be blank");
        if (applicationId == null || applicationId.isBlank()) throw new ValidationException("applicationId", "Application ID must not be blank");
    }
}
