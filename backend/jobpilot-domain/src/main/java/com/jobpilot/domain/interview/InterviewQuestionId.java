package com.jobpilot.domain.interview;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.Objects;
import java.util.UUID;

public final class InterviewQuestionId extends BaseValueObject {

    private final UUID value;

    private InterviewQuestionId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public static InterviewQuestionId generate() { return new InterviewQuestionId(UUID.randomUUID()); }
    public static InterviewQuestionId from(UUID value) { return new InterviewQuestionId(value); }
    public UUID value() { return value; }

    @Override protected Object[] equalityFields() { return new Object[]{value}; }
}
