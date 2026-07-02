package com.jobpilot.domain.identity;

import com.jobpilot.domain.shared.BaseValueObject;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class UserId extends BaseValueObject {

    private final UUID value;

    private UserId(UUID value) {
        this.value = Objects.requireNonNull(value, "userId must not be null");
    }

    public static UserId from(UUID value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(uuidV7());
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
