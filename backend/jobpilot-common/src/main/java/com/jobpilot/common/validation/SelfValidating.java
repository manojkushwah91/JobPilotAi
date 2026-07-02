package com.jobpilot.common.validation;

import com.jobpilot.common.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;

public abstract class SelfValidating {

    protected final List<ValidationException.ValidationError> errors = new ArrayList<>();

    protected void validate(boolean condition, String field, String message) {
        if (!condition) {
            errors.add(new ValidationException.ValidationError(field, message));
        }
    }

    protected void ensureValid() {
        if (!errors.isEmpty()) {
            var copy = List.copyOf(errors);
            errors.clear();
            throw new ValidationException(copy);
        }
    }
}
