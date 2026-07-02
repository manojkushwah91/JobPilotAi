package com.jobpilot.application.application.dto;

import com.jobpilot.common.exception.ValidationException;

public record DeleteApplicationCommand(String applicationId) {
    public DeleteApplicationCommand {
        if (applicationId == null || applicationId.isBlank()) throw new ValidationException("applicationId", "Application ID must not be blank");
    }
}
