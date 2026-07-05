package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.UUID;

public class ResultId extends BaseValueObject {

    private final UUID value;

    private ResultId(UUID value) {
        this.value = value;
    }

    public static ResultId generate() {
        return new ResultId(UUID.randomUUID());
    }

    public static ResultId from(UUID value) {
        return new ResultId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
