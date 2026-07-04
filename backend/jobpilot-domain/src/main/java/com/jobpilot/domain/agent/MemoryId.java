package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.UUID;

public class MemoryId extends BaseValueObject {

    private final UUID value;

    private MemoryId(UUID value) {
        this.value = value;
    }

    public static MemoryId generate() {
        return new MemoryId(UUID.randomUUID());
    }

    public static MemoryId from(UUID value) {
        return new MemoryId(value);
    }

    public static MemoryId fromString(String value) {
        return new MemoryId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
