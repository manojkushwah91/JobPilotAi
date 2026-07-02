package com.jobpilot.application.questionbank.dto;

import com.jobpilot.domain.questionbank.QuestionBankEntry;

import java.util.List;
import java.util.UUID;

public record QuestionBankResponse(
    UUID id,
    String type,
    String category,
    String question,
    int difficulty,
    String expectedAnswer,
    List<String> tags,
    String source,
    UUID companyId,
    int timesUsed
) {
    public static QuestionBankResponse from(QuestionBankEntry entry) {
        return new QuestionBankResponse(
            entry.questionBankId().value(), entry.type(), entry.category(),
            entry.question(), entry.difficulty(), entry.expectedAnswer(),
            entry.tags(), entry.source(), entry.companyId(), entry.timesUsed()
        );
    }
}
