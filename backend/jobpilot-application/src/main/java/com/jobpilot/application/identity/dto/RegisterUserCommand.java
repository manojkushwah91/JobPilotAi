package com.jobpilot.application.identity.dto;

import com.jobpilot.common.exception.ValidationException;
import com.jobpilot.domain.identity.Email;

import java.util.regex.Pattern;

public record RegisterUserCommand(
    String email,
    String password,
    String confirmPassword
) {

    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public RegisterUserCommand {
        if (email == null || email.isBlank()) {
            throw new ValidationException("email", "Email must not be blank");
        }
        try {
            Email.from(email);
        } catch (IllegalArgumentException e) {
            throw new com.jobpilot.common.exception.ValidationException("email", e.getMessage());
        }

        if (password == null || password.length() < 12) {
            throw new ValidationException("password", "Password must be at least 12 characters");
        }
        if (password.length() > 128) {
            throw new ValidationException("password", "Password must not exceed 128 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("password", "Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("password", "Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("password", "Password must contain at least one digit");
        }
        if (!SPECIAL_CHAR.matcher(password).find()) {
            throw new ValidationException("password", "Password must contain at least one special character");
        }

        if (confirmPassword == null || !confirmPassword.equals(password)) {
            throw new ValidationException("confirmPassword", "Passwords do not match");
        }
    }
}
