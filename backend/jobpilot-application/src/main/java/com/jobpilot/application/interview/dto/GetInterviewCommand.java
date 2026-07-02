package com.jobpilot.application.interview.dto;

import com.jobpilot.common.exception.ValidationException;

public record GetInterviewCommand(String sessionId) {
    public GetInterviewCommand {
        if (sessionId == null || sessionId.isBlank()) throw new ValidationException("sessionId", "Session ID must not be blank");
    }
}
