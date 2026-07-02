package com.jobpilot.application.interview.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.UUID;

public record CompleteInterviewCommand(UUID sessionId, int rating, String feedback) {
    public CompleteInterviewCommand {
        if (sessionId == null) throw new ValidationException("sessionId", "Session ID must not be null");
        if (rating < 1 || rating > 5) throw new ValidationException("rating", "Rating must be between 1 and 5");
    }
}
