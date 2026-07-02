package com.jobpilot.application.notification.dto;

import com.jobpilot.domain.notification.Notification;
import com.jobpilot.domain.notification.NotificationChannel;
import com.jobpilot.domain.notification.NotificationStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
    UUID notificationId, UUID userId, String type, NotificationChannel channel,
    String title, String body, Map<String, Object> metadata, NotificationStatus status,
    Instant readAt, Instant sentAt, Instant deliveredAt, Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.notificationId().value(), n.userId(), n.type(), n.channel(),
            n.title(), n.body(), n.metadata(), n.status(), n.readAt(), n.sentAt(), n.deliveredAt(), n.createdAt());
    }
}
