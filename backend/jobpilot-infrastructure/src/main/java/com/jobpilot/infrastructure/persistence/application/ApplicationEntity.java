package com.jobpilot.infrastructure.persistence.application;

import com.jobpilot.domain.application.Application;
import com.jobpilot.domain.application.ApplicationId;
import com.jobpilot.domain.application.ApplicationStatus;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.resume.ResumeId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "applications")
public class ApplicationEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "job_listing_id") private UUID jobListingId;
    @Column(name = "resume_id") private UUID resumeId;
    @Column(name = "cover_letter_id") private UUID coverLetterId;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) private ApplicationStatus status;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_history", columnDefinition = "jsonb") private String statusHistory;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "automation_info", columnDefinition = "jsonb") private String automationInfo;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "salary_offered", columnDefinition = "jsonb") private String salaryOffered;
    @Column(name = "applied_at") private Instant appliedAt;
    @Column(name = "deleted_at") private Instant deletedAt;

    protected ApplicationEntity() {}

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public com.jobpilot.domain.application.ApplicationStatus getStatus() { return status; }
    public UUID getJobListingId() { return jobListingId; }

    public static ApplicationEntity fromDomain(Application app) {
        var e = new ApplicationEntity();
        e.id = app.applicationId().value();
        e.userId = app.userId().value();
        e.jobListingId = app.jobListingId().value();
        e.resumeId = app.resumeId() != null ? app.resumeId().value() : null;
        e.coverLetterId = app.coverLetterId() != null ? app.coverLetterId().value() : null;
        e.status = app.status();
        e.statusHistory = toJson(app.statusHistory());
        e.automationInfo = toJson(app.automationInfo());
        e.salaryOffered = toJson(app.salaryOffered());
        e.appliedAt = app.appliedAt();
        e.deletedAt = app.deletedAt();
        return e;
    }

    @SuppressWarnings("unchecked")
    public Application toDomain() {
        return Application.reconstitute(
            ApplicationId.from(id), UserId.from(userId), JobId.from(jobListingId),
            resumeId != null ? ResumeId.from(resumeId) : null,
            coverLetterId != null ? ApplicationId.from(coverLetterId) : null,
            status, fromJsonList(statusHistory), fromJson(automationInfo),
            fromJson(salaryOffered), appliedAt, deletedAt != null, deletedAt,
            createdAt, updatedAt
        );
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : "[]"; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> fromJson(String json) {
        try { return json != null ? MAPPER.readValue(json, Map.class) : null; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> fromJsonList(String json) {
        try { return json != null ? MAPPER.readValue(json, List.class) : List.of(); }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
}
