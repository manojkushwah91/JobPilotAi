package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.UUID;

public class SessionId extends BaseValueObject {

    private final UUID value;

    private SessionId(UUID value) {
        this.value = value;
    }

    public static SessionId generate() {
        return new SessionId(UUID.randomUUID());
    }

    public static SessionId from(UUID value) {
        return new SessionId(value);
    }

    public static SessionId fromString(String value) {
        return new SessionId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
