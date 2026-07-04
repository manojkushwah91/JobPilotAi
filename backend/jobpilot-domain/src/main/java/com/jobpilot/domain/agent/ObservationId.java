package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.UUID;

public class ObservationId extends BaseValueObject {

    private final UUID value;

    private ObservationId(UUID value) {
        this.value = value;
    }

    public static ObservationId generate() {
        return new ObservationId(UUID.randomUUID());
    }

    public static ObservationId from(UUID value) {
        return new ObservationId(value);
    }

    public static ObservationId fromString(String value) {
        return new ObservationId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
