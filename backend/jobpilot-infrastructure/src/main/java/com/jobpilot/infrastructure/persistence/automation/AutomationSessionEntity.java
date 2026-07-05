package com.jobpilot.infrastructure.persistence.automation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.automation.AutomationSessionId;
import com.jobpilot.domain.automation.AutomationStatus;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "automation_sessions")
public class AutomationSessionEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "application_id") private UUID applicationId;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "step") private String currentStep;
    @Column(name = "progress") private int progress;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "screenshots", columnDefinition = "jsonb") private String screenshots;
    @Column(name = "error_message", columnDefinition = "text") private String errorMessage;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;

    protected AutomationSessionEntity() {}

    public static AutomationSessionEntity fromDomain(AutomationSession session) {
        var e = new AutomationSessionEntity();
        e.id = session.sessionId().value();
        e.userId = session.userId();
        e.applicationId = session.applicationId();
        e.status = session.status().name();
        e.currentStep = session.currentStep();
        e.progress = session.progress();
        e.screenshots = toJson(session.screenshots());
        e.errorMessage = session.errorMessage();
        e.startedAt = session.startedAt();
        e.completedAt = session.completedAt();
        return e;
    }

    public AutomationSession toDomain() {
        return AutomationSession.reconstitute(
            AutomationSessionId.from(id), userId, applicationId,
            AutomationStatus.valueOf(status), currentStep, progress,
            fromJsonList(screenshots), errorMessage,
            startedAt, completedAt, createdAt
        );
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : "[]"; }
        catch (Exception e) { throw new RuntimeException("JSON serialization error", e); }
    }

    private static List<String> fromJsonList(String json) {
        try { return json != null ? MAPPER.readValue(json, new TypeReference<List<String>>() {}) : List.of(); }
        catch (Exception e) { throw new RuntimeException("JSON deserialization error", e); }
    }
}
