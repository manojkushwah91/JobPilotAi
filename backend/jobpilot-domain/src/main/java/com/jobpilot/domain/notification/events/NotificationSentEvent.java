package com.jobpilot.domain.notification.events;

import com.jobpilot.domain.notification.NotificationId;
import com.jobpilot.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record NotificationSentEvent(
    UUID eventId, Instant occurredAt, NotificationId notificationId, UUID userId
) implements DomainEvent {
    public NotificationSentEvent(NotificationId notificationId, UUID userId) {
        this(UUID.randomUUID(), Instant.now(), notificationId, userId);
    }
    @Override public String aggregateType() { return "Notification"; }
    @Override public UUID aggregateId() { return notificationId.value(); }
    @Override public String eventType() { return "notification.sent"; }
}
