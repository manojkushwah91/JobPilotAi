package com.jobpilot.domain.notification;

import com.jobpilot.domain.shared.BaseValueObject;
import java.util.Objects;
import java.util.UUID;

public final class NotificationId extends BaseValueObject {
    private final UUID value;
    private NotificationId(UUID value) { this.value = Objects.requireNonNull(value, "value must not be null"); }
    public static NotificationId generate() { return new NotificationId(UUID.randomUUID()); }
    public static NotificationId from(UUID value) { return new NotificationId(value); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
}
