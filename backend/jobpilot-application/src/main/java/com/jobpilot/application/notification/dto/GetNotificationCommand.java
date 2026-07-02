package com.jobpilot.application.notification.dto;

import com.jobpilot.common.exception.ValidationException;

public record GetNotificationCommand(String notificationId) {
    public GetNotificationCommand {
        if (notificationId == null || notificationId.isBlank()) throw new ValidationException("notificationId", "Notification ID must not be blank");
    }
}
