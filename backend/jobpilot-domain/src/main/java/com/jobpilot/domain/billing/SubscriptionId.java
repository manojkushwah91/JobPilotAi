package com.jobpilot.domain.billing;

import com.jobpilot.domain.shared.BaseValueObject;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SubscriptionId extends BaseValueObject {
    private final UUID value;

    private SubscriptionId(UUID value) {
        this.value = Objects.requireNonNull(value, "subscriptionId must not be null");
    }

    public static SubscriptionId from(UUID value) { return new SubscriptionId(value); }

    public static SubscriptionId generate() { return new SubscriptionId(uuidV7()); }

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
