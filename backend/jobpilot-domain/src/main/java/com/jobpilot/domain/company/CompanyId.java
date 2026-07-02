package com.jobpilot.domain.company;

import com.jobpilot.domain.shared.BaseValueObject;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class CompanyId extends BaseValueObject {
    private final UUID value;
    private CompanyId(UUID value) { this.value = Objects.requireNonNull(value, "companyId must not be null"); }
    public static CompanyId from(UUID value) { return new CompanyId(value); }
    public static CompanyId generate() { return new CompanyId(uuidV7()); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
    private static UUID uuidV7() {
        var now = Instant.now().toEpochMilli();
        var uuid = UUID.randomUUID();
        return new UUID((now << 16) | (uuid.getMostSignificantBits() & 0x0000_0000_0000_FFFFL), uuid.getLeastSignificantBits());
    }
}
