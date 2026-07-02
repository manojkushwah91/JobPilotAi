package com.jobpilot.domain.job;

import com.jobpilot.domain.shared.BaseValueObject;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class JobId extends BaseValueObject {
    private final UUID value;
    private JobId(UUID value) { this.value = Objects.requireNonNull(value, "jobId must not be null"); }
    public static JobId from(UUID value) { return new JobId(value); }
    public static JobId generate() { return new JobId(uuidV7()); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
    private static UUID uuidV7() {
        var now = Instant.now().toEpochMilli();
        var uuid = UUID.randomUUID();
        return new UUID((now << 16) | (uuid.getMostSignificantBits() & 0x0000_0000_0000_FFFFL), uuid.getLeastSignificantBits());
    }
}
