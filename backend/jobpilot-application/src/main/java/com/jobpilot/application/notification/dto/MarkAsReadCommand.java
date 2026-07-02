package com.jobpilot.application.notification.dto;

import com.jobpilot.common.exception.ValidationException;

public record MarkAsReadCommand(String notificationId) {
    public MarkAsReadCommand {
        if (notificationId == null || notificationId.isBlank()) throw new ValidationException("notificationId", "Notification ID must not be blank");
    }
}
