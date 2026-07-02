package com.jobpilot.application.notification.dto;

import com.jobpilot.common.exception.ValidationException;
import com.jobpilot.domain.notification.NotificationChannel;
import com.jobpilot.domain.notification.NotificationStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SendNotificationCommand(UUID userId, String type, NotificationChannel channel, String title, String body, Map<String, Object> metadata) {
    public SendNotificationCommand {
        if (userId == null) throw new ValidationException("userId", "User ID must not be null");
        if (type == null || type.isBlank()) throw new ValidationException("type", "Type must not be blank");
        if (channel == null) throw new ValidationException("channel", "Channel must not be null");
        if (title == null || title.isBlank()) throw new ValidationException("title", "Title must not be blank");
        if (body == null || body.isBlank()) throw new ValidationException("body", "Body must not be blank");
    }
}
