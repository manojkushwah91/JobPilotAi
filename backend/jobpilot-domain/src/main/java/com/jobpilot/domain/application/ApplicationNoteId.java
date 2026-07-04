package com.jobpilot.domain.application;

import com.jobpilot.domain.shared.BaseValueObject;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ApplicationNoteId extends BaseValueObject {
    private final UUID value;
    private ApplicationNoteId(UUID value) { this.value = Objects.requireNonNull(value, "noteId must not be null"); }
    public static ApplicationNoteId from(UUID value) { return new ApplicationNoteId(value); }
    public static ApplicationNoteId generate() { return new ApplicationNoteId(uuidV7()); }
    public UUID value() { return value; }
    @Override protected Object[] equalityFields() { return new Object[]{value}; }
    private static UUID uuidV7() {
        var now = Instant.now().toEpochMilli();
        var uuid = UUID.randomUUID();
        return new UUID((now << 16) | (uuid.getMostSignificantBits() & 0x0000_0000_0000_FFFFL), uuid.getLeastSignificantBits());
    }
}
