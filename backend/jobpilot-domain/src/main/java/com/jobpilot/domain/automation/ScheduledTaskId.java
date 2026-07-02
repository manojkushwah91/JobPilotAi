package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseValueObject;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ScheduledTaskId extends BaseValueObject {
    private final UUID value;

    private ScheduledTaskId(UUID value) {
        this.value = Objects.requireNonNull(value, "scheduledTaskId must not be null");
    }

    public static ScheduledTaskId from(UUID value) { return new ScheduledTaskId(value); }

    public static ScheduledTaskId generate() { return new ScheduledTaskId(uuidV7()); }

    public UUID value() { return value; }

    @Override protected Object[] equalityFields() { return new Object[]{value}; }

    private static UUID uuidV7() {
        var now = Instant.now().toEpochMilli();
        var uuid = UUID.randomUUID();
        return new UUID(
            (now << 16) | (uuid.getMostSignificantBits() & 0x0000_0000_0000_FFFFL),
            uuid.getLeastSignificantBits()
        );
    }
}
