package com.jobpilot.domain.questionbank;

import com.jobpilot.domain.shared.BaseValueObject;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class QuestionBankId extends BaseValueObject {

    private final UUID value;

    private QuestionBankId(UUID value) {
        this.value = Objects.requireNonNull(value, "questionBankId must not be null");
    }

    public static QuestionBankId from(UUID value) {
        return new QuestionBankId(value);
    }

    public static QuestionBankId generate() {
        return new QuestionBankId(uuidV7());
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
