package com.jobpilot.application.questionbank.dto;

import com.jobpilot.common.exception.ValidationException;

public record SearchQuestionsCommand(
    String query,
    String category,
    Integer difficulty,
    String type,
    int page,
    int size
) {
    public SearchQuestionsCommand {
        if (query != null && query.isBlank()) throw new ValidationException("query", "Query must not be blank");
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;
    }
}
