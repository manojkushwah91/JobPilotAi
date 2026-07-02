package com.jobpilot.application.coverletter.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.UUID;

public record CreateCoverLetterCommand(
    UUID userId,
    String title,
    String companyName,
    String jobTitle,
    String content,
    String tone
) {
    public CreateCoverLetterCommand {
        if (userId == null) {
            throw new ValidationException("userId", "User ID must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new ValidationException("title", "Title must not be blank");
        }
        if (title.length() > 255) {
            throw new ValidationException("title", "Title must not exceed 255 characters");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new ValidationException("companyName", "Company name must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new ValidationException("content", "Content must not be blank");
        }
        if (tone == null) {
            tone = "PROFESSIONAL";
        }
    }
}
