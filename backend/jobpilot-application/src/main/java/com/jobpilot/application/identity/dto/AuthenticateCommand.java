package com.jobpilot.application.identity.dto;

import com.jobpilot.common.exception.ValidationException;

public record AuthenticateCommand(
    String email,
    String password
) {

    public AuthenticateCommand {
        if (email == null || email.isBlank()) {
            throw new ValidationException("email", "Email must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("password", "Password must not be blank");
        }
    }
}
