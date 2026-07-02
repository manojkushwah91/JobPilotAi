package com.jobpilot.application.application.dto;

import com.jobpilot.common.exception.ValidationException;

public record ApplyCommand(String userId, String jobListingId, String resumeId) {
    public ApplyCommand {
        if (userId == null || userId.isBlank()) throw new ValidationException("userId", "User ID must not be blank");
        if (jobListingId == null || jobListingId.isBlank()) throw new ValidationException("jobListingId", "Job listing ID must not be blank");
        if (resumeId == null || resumeId.isBlank()) throw new ValidationException("resumeId", "Resume ID must not be blank");
    }
}
