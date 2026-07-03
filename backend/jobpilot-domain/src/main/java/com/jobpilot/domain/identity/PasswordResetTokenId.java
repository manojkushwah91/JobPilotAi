package com.jobpilot.domain.identity;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.Objects;
import java.util.UUID;

public final class PasswordResetTokenId extends BaseValueObject {

    private final UUID value;

    private PasswordResetTokenId(UUID value) {
        this.value = Objects.requireNonNull(value, "passwordResetTokenId must not be null");
    }

    public static PasswordResetTokenId from(UUID value) {
        return new PasswordResetTokenId(value);
    }

    public static PasswordResetTokenId generate() {
        return new PasswordResetTokenId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
