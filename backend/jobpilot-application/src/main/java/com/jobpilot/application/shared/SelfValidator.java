package com.jobpilot.application.shared;

import com.jobpilot.common.exception.ValidationException;

public interface SelfValidator {
    default void selfValidate() {
    }

    default void fail(String field, String message) {
        throw new ValidationException(field, message);
    }
}
