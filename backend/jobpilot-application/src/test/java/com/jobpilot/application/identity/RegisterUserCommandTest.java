package com.jobpilot.application.identity;

import com.jobpilot.application.identity.dto.RegisterUserCommand;
import com.jobpilot.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserCommandTest {

    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "", "ValidPass1!", "ValidPass1!"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "not-email", "ValidPass1!", "ValidPass1!"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectShortPassword() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "test@test.com", "Short1!", "Short1!"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectPasswordWithoutUppercase() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "test@test.com", "nouppercase1!", "nouppercase1!"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectPasswordWithoutLowercase() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "test@test.com", "NOLOWERCASE1!", "NOLOWERCASE1!"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectPasswordWithoutDigit() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "test@test.com", "NoDigitPass!", "NoDigitPass!"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectPasswordWithoutSpecialChar() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "test@test.com", "NoSpecialChar1", "NoSpecialChar1"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectMismatchedPasswords() {
        assertThatThrownBy(() -> new RegisterUserCommand(null, "test@test.com", "ValidPass1!", "Different1!"))
            .isInstanceOf(ValidationException.class);
    }
}
