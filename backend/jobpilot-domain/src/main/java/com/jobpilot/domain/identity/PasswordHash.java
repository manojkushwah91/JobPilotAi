package com.jobpilot.domain.identity;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.regex.Pattern;

public final class PasswordHash extends BaseValueObject {

    private static final Pattern BCRYPT_PATTERN =
        Pattern.compile("^\\$2[abxy]\\$\\d{2}\\$[A-Za-z0-9./]{53}$");

    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash from(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        if (!BCRYPT_PATTERN.matcher(hash).matches()) {
            throw new IllegalArgumentException("Invalid bcrypt hash format");
        }
        return new PasswordHash(hash);
    }

    public String value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
