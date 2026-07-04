package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.UUID;

public class MissionId extends BaseValueObject {

    private final UUID value;

    private MissionId(UUID value) {
        this.value = value;
    }

    public static MissionId generate() {
        return new MissionId(UUID.randomUUID());
    }

    public static MissionId from(UUID value) {
        return new MissionId(value);
    }

    public static MissionId fromString(String value) {
        return new MissionId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
