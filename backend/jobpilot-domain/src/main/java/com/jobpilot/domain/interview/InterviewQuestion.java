package com.jobpilot.domain.interview;

import java.util.Map;
import java.util.Objects;

public final class InterviewQuestion {

    private final String question;
    private final String expectedAnswer;
    private final int difficulty;
    private final String category;
    private final Map<String, Object> metadata;

    public InterviewQuestion(String question, String expectedAnswer, int difficulty,
                              String category, Map<String, Object> metadata) {
        if (question == null || question.isBlank()) throw new IllegalArgumentException("question must not be blank");
        this.question = question;
        this.expectedAnswer = expectedAnswer;
        this.difficulty = difficulty;
        this.category = category;
        this.metadata = metadata;
    }

    public String question() { return question; }
    public String expectedAnswer() { return expectedAnswer; }
    public int difficulty() { return difficulty; }
    public String category() { return category; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterviewQuestion that = (InterviewQuestion) o;
        return difficulty == that.difficulty && Objects.equals(question, that.question)
            && Objects.equals(expectedAnswer, that.expectedAnswer) && Objects.equals(category, that.category)
            && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() { return Objects.hash(question, expectedAnswer, difficulty, category, metadata); }
}
