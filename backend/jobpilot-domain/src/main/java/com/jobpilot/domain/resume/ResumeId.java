package com.jobpilot.domain.resume;

import com.jobpilot.domain.shared.BaseValueObject;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ResumeId extends BaseValueObject {

    private final UUID value;

    private ResumeId(UUID value) {
        this.value = Objects.requireNonNull(value, "resumeId must not be null");
    }

    public static ResumeId from(UUID value) {
        return new ResumeId(value);
    }

    public static ResumeId generate() {
        return new ResumeId(uuidV7());
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }

    private static UUID uuidV7() {
        var now = Instant.now().toEpochMilli();
        var uuid = UUID.randomUUID();
        return new UUID(
            (now << 16) | (uuid.getMostSignificantBits() & 0x0000_0000_0000_FFFFL),
            uuid.getLeastSignificantBits()
        );
    }
}
