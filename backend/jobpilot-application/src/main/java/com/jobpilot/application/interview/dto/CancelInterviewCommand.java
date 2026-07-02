package com.jobpilot.application.interview.dto;

import com.jobpilot.common.exception.ValidationException;

public record CancelInterviewCommand(String sessionId, String reason) {
    public CancelInterviewCommand {
        if (sessionId == null || sessionId.isBlank()) throw new ValidationException("sessionId", "Session ID must not be blank");
    }
}
