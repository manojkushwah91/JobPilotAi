package com.jobpilot.domain.identity;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.regex.Pattern;

public final class Email extends BaseValueObject {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        var normalized = raw.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + raw);
        }
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Email must not exceed 255 characters");
        }
        return new Email(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
