package com.jobpilot.application.automation.dto;

import com.jobpilot.common.exception.ValidationException;

public record ConfirmActionCommand(String sessionId) {
    public ConfirmActionCommand {
        if (sessionId == null || sessionId.isBlank()) throw new ValidationException("sessionId", "Session ID must not be blank");
    }
}
