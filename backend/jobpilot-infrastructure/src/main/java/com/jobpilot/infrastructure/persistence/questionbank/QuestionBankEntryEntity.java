package com.jobpilot.infrastructure.persistence.questionbank;

import com.jobpilot.domain.questionbank.QuestionBankEntry;
import com.jobpilot.domain.questionbank.QuestionBankId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interview_question_bank")
public class QuestionBankEntryEntity extends BaseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "question", nullable = false, columnDefinition = "text")
    private String question;

    @Column(name = "difficulty", nullable = false)
    private int difficulty;

    @Column(name = "expected_answer", columnDefinition = "text")
    private String expectedAnswer;

    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags;

    @Column(name = "source")
    private String source;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "times_used", nullable = false)
    private int timesUsed;

    protected QuestionBankEntryEntity() {}

    public static QuestionBankEntryEntity fromDomain(QuestionBankEntry entry) {
        var e = new QuestionBankEntryEntity();
        e.id = entry.questionBankId().value();
        e.type = entry.type();
        e.category = entry.category();
        e.question = entry.question();
        e.difficulty = entry.difficulty();
        e.expectedAnswer = entry.expectedAnswer();
        e.tags = toJson(entry.tags());
        e.source = entry.source();
        e.companyId = entry.companyId();
        e.timesUsed = entry.timesUsed();
        return e;
    }

    public QuestionBankEntry toDomain() {
        return QuestionBankEntry.reconstitute(
            QuestionBankId.from(id), type, category, question, difficulty,
            expectedAnswer, fromJsonList(tags), source, companyId, timesUsed
        );
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : "[]"; }
        catch (Exception e) { throw new RuntimeException("JSON serialization error", e); }
    }

    @SuppressWarnings("unchecked")
    private static List<String> fromJsonList(String json) {
        try { return json != null ? MAPPER.readValue(json, List.class) : List.of(); }
        catch (Exception e) { throw new RuntimeException("JSON parse error", e); }
    }
}
