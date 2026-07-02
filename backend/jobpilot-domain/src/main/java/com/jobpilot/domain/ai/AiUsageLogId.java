package com.jobpilot.domain.ai;

import com.jobpilot.domain.shared.BaseValueObject;
import java.util.Objects;
import java.util.UUID;

public final class AiUsageLogId extends BaseValueObject {
    private final UUID value;
    private AiUsageLogId(UUID value) { this.value = Objects.requireNonNull(value, "value must not be null"); }
    public static AiUsageLogId generate() { return new AiUsageLogId(UUID.randomUUID()); }
    public static AiUsageLogId from(UUID value) { return new AiUsageLogId(value); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
}
