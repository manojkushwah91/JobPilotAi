package com.jobpilot.domain.interview;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.Objects;
import java.util.UUID;

public final class InterviewSessionId extends BaseValueObject {

    private final UUID value;

    private InterviewSessionId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public static InterviewSessionId generate() { return new InterviewSessionId(UUID.randomUUID()); }
    public static InterviewSessionId from(UUID value) { return new InterviewSessionId(value); }
    public UUID value() { return value; }

    @Override protected Object[] equalityFields() { return new Object[]{value}; }
}
