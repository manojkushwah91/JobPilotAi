package com.jobpilot.domain.billing;

import com.jobpilot.domain.shared.BaseValueObject;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class InvoiceId extends BaseValueObject {
    private final UUID value;
    private InvoiceId(UUID value) { this.value = Objects.requireNonNull(value, "invoiceId must not be null"); }
    public static InvoiceId from(UUID value) { return new InvoiceId(value); }
    public static InvoiceId generate() { return new InvoiceId(uuidV7()); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
    private static UUID uuidV7() {
        var now = Instant.now().toEpochMilli();
        var uuid = UUID.randomUUID();
        return new UUID((now << 16) | (uuid.getMostSignificantBits() & 0x0000_0000_0000_FFFFL), uuid.getLeastSignificantBits());
    }
}
