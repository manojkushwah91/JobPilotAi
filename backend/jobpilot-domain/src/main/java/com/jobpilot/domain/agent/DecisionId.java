package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.UUID;

public class DecisionId extends BaseValueObject {

    private final UUID value;

    private DecisionId(UUID value) {
        this.value = value;
    }

    public static DecisionId generate() {
        return new DecisionId(UUID.randomUUID());
    }

    public static DecisionId from(UUID value) {
        return new DecisionId(value);
    }

    public static DecisionId fromString(String value) {
        return new DecisionId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
