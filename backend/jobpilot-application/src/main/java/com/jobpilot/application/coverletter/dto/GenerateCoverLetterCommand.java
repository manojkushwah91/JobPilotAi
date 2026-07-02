package com.jobpilot.application.coverletter.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.UUID;

public record GenerateCoverLetterCommand(
    UUID userId,
    String companyName,
    String jobTitle,
    String tone,
    String recipientName
) {
    public GenerateCoverLetterCommand {
        if (userId == null) {
            throw new ValidationException("userId", "User ID must not be null");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new ValidationException("companyName", "Company name must not be blank");
        }
        if (companyName.length() > 255) {
            throw new ValidationException("companyName", "Company name must not exceed 255 characters");
        }
        if (jobTitle == null || jobTitle.isBlank()) {
            throw new ValidationException("jobTitle", "Job title must not be blank");
        }
        if (tone == null) {
            tone = "PROFESSIONAL";
        }
    }
}
