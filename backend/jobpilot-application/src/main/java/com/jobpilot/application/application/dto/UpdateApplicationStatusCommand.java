package com.jobpilot.application.application.dto;

import com.jobpilot.common.exception.ValidationException;

public record UpdateApplicationStatusCommand(String applicationId, String status) {
    public UpdateApplicationStatusCommand {
        if (applicationId == null || applicationId.isBlank()) throw new ValidationException("applicationId", "Application ID must not be blank");
        if (status == null || status.isBlank()) throw new ValidationException("status", "Status must not be blank");
    }
}
