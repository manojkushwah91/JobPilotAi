package com.jobpilot.domain.coverletter;

import com.jobpilot.domain.shared.BaseValueObject;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class CoverLetterId extends BaseValueObject {

    private final UUID value;

    private CoverLetterId(UUID value) {
        this.value = Objects.requireNonNull(value, "coverLetterId must not be null");
    }

    public static CoverLetterId from(UUID value) {
        return new CoverLetterId(value);
    }

    public static CoverLetterId generate() {
        return new CoverLetterId(uuidV7());
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
