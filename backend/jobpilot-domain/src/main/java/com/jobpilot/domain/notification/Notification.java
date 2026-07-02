package com.jobpilot.domain.notification;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.notification.events.NotificationSentEvent;
import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Notification extends BaseAggregateRoot {

    private final NotificationId notificationId;
    private UUID userId;
    private String type;
    private NotificationChannel channel;
    private String title;
    private String body;
    private Map<String, Object> metadata;
    private NotificationStatus status;
    private Instant readAt;
    private Instant sentAt;
    private Instant deliveredAt;
    private final Instant createdAt;

    private Notification(NotificationId notificationId, UUID userId, String type, NotificationChannel channel,
                          String title, String body) {
        super(notificationId.value());
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.body = body;
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static Notification create(NotificationId notificationId, UUID userId, String type,
                                       NotificationChannel channel, String title, String body) {
        return new Notification(notificationId, userId, type, channel, title, body);
    }

    public static Notification reconstitute(NotificationId notificationId, UUID userId, String type,
            NotificationChannel channel, String title, String body, Map<String, Object> metadata,
            NotificationStatus status, Instant readAt, Instant sentAt, Instant deliveredAt, Instant createdAt) {
        var n = new Notification(notificationId, userId, type, channel, title, body);
        n.metadata = metadata;
        n.status = status;
        n.readAt = readAt;
        n.sentAt = sentAt;
        n.deliveredAt = deliveredAt;
        return n;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        registerEvent(new NotificationSentEvent(notificationId, userId));
    }

    public void markDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public NotificationId notificationId() { return notificationId; }
    public UUID userId() { return userId; }
    public String type() { return type; }
    public NotificationChannel channel() { return channel; }
    public String title() { return title; }
    public String body() { return body; }
    public Map<String, Object> metadata() { return metadata; }
    public NotificationStatus status() { return status; }
    public Instant readAt() { return readAt; }
    public Instant sentAt() { return sentAt; }
    public Instant deliveredAt() { return deliveredAt; }
    public Instant createdAt() { return createdAt; }
}
