package com.jobpilot.application.resume.dto;

import com.jobpilot.common.exception.ValidationException;

public record ListResumesCommand(String userId) {
    public ListResumesCommand {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("userId", "User ID must not be blank");
        }
    }
}
