package com.jobpilot.application.resume.dto;

import com.jobpilot.common.exception.ValidationException;

public record DeleteResumeCommand(String resumeId, String userId) {
    public DeleteResumeCommand {
        if (resumeId == null || resumeId.isBlank()) {
            throw new ValidationException("resumeId", "Resume ID must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("userId", "User ID must not be blank");
        }
    }
}
