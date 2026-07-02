package com.jobpilot.application.application.dto;

import com.jobpilot.common.exception.ValidationException;

public record ListApplicationsCommand(String userId) {
    public ListApplicationsCommand {
        if (userId == null || userId.isBlank()) throw new ValidationException("userId", "User ID must not be blank");
    }
}
