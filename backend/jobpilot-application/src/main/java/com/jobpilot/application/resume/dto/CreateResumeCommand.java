package com.jobpilot.application.resume.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.List;

public record CreateResumeCommand(
    String userId,
    String title,
    List<SectionDto> sections
) {

    public CreateResumeCommand {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("userId", "User ID must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new ValidationException("title", "Title must not be blank");
        }
        if (title.length() > 255) {
            throw new ValidationException("title", "Title must not exceed 255 characters");
        }
        if (sections == null) {
            sections = List.of();
        }
    }
}
