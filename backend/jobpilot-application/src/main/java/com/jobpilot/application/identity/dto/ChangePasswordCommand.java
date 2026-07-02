package com.jobpilot.application.identity.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.regex.Pattern;

public record ChangePasswordCommand(
    String userId,
    String currentPassword,
    String newPassword,
    String confirmNewPassword
) {

    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public ChangePasswordCommand {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("userId", "User ID must not be blank");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new ValidationException("currentPassword", "Current password must not be blank");
        }
        if (newPassword == null || newPassword.length() < 12) {
            throw new ValidationException("newPassword", "New password must be at least 12 characters");
        }
        if (newPassword.length() > 128) {
            throw new ValidationException("newPassword", "New password must not exceed 128 characters");
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new ValidationException("newPassword", "New password must contain at least one uppercase letter");
        }
        if (!newPassword.matches(".*[a-z].*")) {
            throw new ValidationException("newPassword", "New password must contain at least one lowercase letter");
        }
        if (!newPassword.matches(".*\\d.*")) {
            throw new ValidationException("newPassword", "New password must contain at least one digit");
        }
        if (!SPECIAL_CHAR.matcher(newPassword).find()) {
            throw new ValidationException("newPassword", "New password must contain at least one special character");
        }
        if (confirmNewPassword == null || !confirmNewPassword.equals(newPassword)) {
            throw new ValidationException("confirmNewPassword", "Passwords do not match");
        }
    }
}
