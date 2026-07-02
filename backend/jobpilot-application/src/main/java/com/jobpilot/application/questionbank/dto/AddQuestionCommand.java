package com.jobpilot.application.questionbank.dto;

import com.jobpilot.common.exception.ValidationException;

public record AddQuestionCommand(
    String type,
    String category,
    String question,
    int difficulty
) {
    public AddQuestionCommand {
        if (type == null || type.isBlank()) throw new ValidationException("type", "Type must not be blank");
        if (category == null || category.isBlank()) throw new ValidationException("category", "Category must not be blank");
        if (question == null || question.isBlank()) throw new ValidationException("question", "Question must not be blank");
    }
}
