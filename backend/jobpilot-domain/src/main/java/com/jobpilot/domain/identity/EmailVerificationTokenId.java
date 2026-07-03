package com.jobpilot.domain.identity;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.Objects;
import java.util.UUID;

public final class EmailVerificationTokenId extends BaseValueObject {

    private final UUID value;

    private EmailVerificationTokenId(UUID value) {
        this.value = Objects.requireNonNull(value, "emailVerificationTokenId must not be null");
    }

    public static EmailVerificationTokenId from(UUID value) {
        return new EmailVerificationTokenId(value);
    }

    public static EmailVerificationTokenId generate() {
        return new EmailVerificationTokenId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
