package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseValueObject;
import java.util.Objects;
import java.util.UUID;

public final class AutomationSessionId extends BaseValueObject {
    private final UUID value;
    private AutomationSessionId(UUID value) { this.value = Objects.requireNonNull(value, "sessionId must not be null"); }
    public static AutomationSessionId from(UUID value) { return new AutomationSessionId(value); }
    public static AutomationSessionId generate() { return new AutomationSessionId(UUID.randomUUID()); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
}
