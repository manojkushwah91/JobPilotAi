package com.jobpilot.application.application.dto;

import com.jobpilot.common.exception.ValidationException;

public record GetApplicationCommand(String applicationId) {
    public GetApplicationCommand {
        if (applicationId == null || applicationId.isBlank()) throw new ValidationException("applicationId", "Application ID must not be blank");
    }
}
