package com.jobpilot.application.automation.dto;

import com.jobpilot.common.exception.ValidationException;
import com.jobpilot.domain.notification.NotificationChannel;
import java.time.Instant;
import java.util.UUID;

public record ScheduleNotificationCommand(
    UUID userId, String type, NotificationChannel channel,
    String title, String body, Instant scheduledAt
) {
    public ScheduleNotificationCommand {
        if (userId == null) throw new ValidationException("userId", "User ID must not be null");
        if (type == null || type.isBlank()) throw new ValidationException("type", "Type must not be blank");
        if (channel == null) throw new ValidationException("channel", "Channel must not be null");
        if (title == null || title.isBlank()) throw new ValidationException("title", "Title must not be blank");
        if (body == null || body.isBlank()) throw new ValidationException("body", "Body must not be blank");
        if (scheduledAt == null) throw new ValidationException("scheduledAt", "Scheduled at must not be null");
    }
}
