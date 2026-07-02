package com.jobpilot.common.validation;

import com.jobpilot.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelfValidatingTest {

    @Test
    void shouldPassWhenNoErrors() {
        var validator = new TestValidator();
        validator.check(true, "field", "must not fail");
        validator.verify();
    }

    @Test
    void shouldThrowOnValidationFailure() {
        var validator = new TestValidator();
        validator.check(false, "email", "must not be blank");
        assertThatThrownBy(validator::verify)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldCollectMultipleErrors() {
        var validator = new TestValidator();
        validator.check(false, "name", "required");
        validator.check(false, "email", "invalid format");

        assertThatThrownBy(validator::verify)
            .isInstanceOfSatisfying(ValidationException.class, ex -> {
                assertThat(ex.getErrors()).hasSize(2);
            });
    }

    @Test
    void shouldClearErrorsAfterThrow() {
        var validator = new TestValidator();
        validator.check(false, "name", "required");

        assertThatThrownBy(validator::verify).isInstanceOf(ValidationException.class);

        validator.check(true, "name", "required");
        validator.verify();
    }

    static class TestValidator extends SelfValidating {
        void check(boolean condition, String field, String message) {
            validate(condition, field, message);
        }
        void verify() {
            ensureValid();
        }
    }
}
