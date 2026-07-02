package com.jobpilot.infrastructure.persistence.notification;

import com.jobpilot.domain.notification.*;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "type", nullable = false) private String type;
    @Column(name = "channel", nullable = false) private String channel;
    @Column(name = "title", nullable = false) private String title;
    @Column(name = "body", nullable = false, columnDefinition = "text") private String body;
    @Column(name = "metadata", columnDefinition = "jsonb") private String metadata;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "delivered_at") private Instant deliveredAt;

    protected NotificationEntity() {}

    public static NotificationEntity fromDomain(Notification n) {
        var e = new NotificationEntity();
        e.id = n.notificationId().value();
        e.userId = n.userId();
        e.type = n.type();
        e.channel = n.channel().name();
        e.title = n.title();
        e.body = n.body();
        e.metadata = toJson(n.metadata());
        e.status = n.status().name();
        e.readAt = n.readAt();
        e.sentAt = n.sentAt();
        e.deliveredAt = n.deliveredAt();
        return e;
    }

    @SuppressWarnings("unchecked")
    public Notification toDomain() {
        return Notification.reconstitute(NotificationId.from(id), userId, type,
            NotificationChannel.valueOf(channel), title, body, fromJson(metadata),
            NotificationStatus.valueOf(status), readAt, sentAt, deliveredAt, createdAt);
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : "{}"; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> fromJson(String json) {
        try { return (json != null && !json.isBlank()) ? MAPPER.readValue(json, Map.class) : null; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
}
