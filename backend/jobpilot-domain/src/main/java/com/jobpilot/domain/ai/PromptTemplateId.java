package com.jobpilot.domain.ai;

import com.jobpilot.domain.shared.BaseValueObject;
import java.util.Objects;
import java.util.UUID;

public final class PromptTemplateId extends BaseValueObject {
    private final UUID value;
    private PromptTemplateId(UUID value) { this.value = Objects.requireNonNull(value, "value must not be null"); }
    public static PromptTemplateId generate() { return new PromptTemplateId(UUID.randomUUID()); }
    public static PromptTemplateId from(UUID value) { return new PromptTemplateId(value); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
}
