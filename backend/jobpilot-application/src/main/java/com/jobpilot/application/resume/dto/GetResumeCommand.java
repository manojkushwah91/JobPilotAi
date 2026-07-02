package com.jobpilot.application.resume.dto;

import com.jobpilot.common.exception.ValidationException;

public record GetResumeCommand(String resumeId) {
    public GetResumeCommand {
        if (resumeId == null || resumeId.isBlank()) {
            throw new ValidationException("resumeId", "Resume ID must not be blank");
        }
    }
}
