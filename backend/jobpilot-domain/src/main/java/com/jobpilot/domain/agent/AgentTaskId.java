package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.UUID;

public class AgentTaskId extends BaseValueObject {

    private final UUID value;

    private AgentTaskId(UUID value) {
        this.value = value;
    }

    public static AgentTaskId generate() {
        return new AgentTaskId(UUID.randomUUID());
    }

    public static AgentTaskId from(UUID value) {
        return new AgentTaskId(value);
    }

    public static AgentTaskId fromString(String value) {
        return new AgentTaskId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
