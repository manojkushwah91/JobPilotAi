package com.jobpilot.infrastructure.persistence.interview;

import com.jobpilot.domain.interview.*;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interview_sessions")
public class InterviewSessionEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "company_id") private UUID companyId;
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "type", nullable = false) private String type;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "scheduled_at", nullable = false) private Instant scheduledAt;
    @Column(name = "duration_minutes") private Integer durationMinutes;
    @Column(name = "interviewer_name") private String interviewerName;
    @Column(name = "interview_round") private Integer interviewRound;
    @Column(name = "location") private String location;
    @Column(name = "meeting_link") private String meetingLink;
    @Column(name = "notes", columnDefinition = "text") private String notes;
    @Column(name = "feedback", columnDefinition = "text") private String feedback;
    @Column(name = "rating") private Integer rating;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", columnDefinition = "jsonb") private String questions;
    @Column(name = "deleted") private boolean deleted;
    @Column(name = "deleted_at") private Instant deletedAt;

    protected InterviewSessionEntity() {}

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCompanyId() { return companyId; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public Integer getRating() { return rating; }
    public Instant getScheduledAt() { return scheduledAt; }

    public static InterviewSessionEntity fromDomain(InterviewSession s) {
        var e = new InterviewSessionEntity();
        e.id = s.sessionId().value();
        e.userId = s.userId();
        e.companyId = s.companyId();
        e.jobId = s.jobId();
        e.type = s.type();
        e.status = s.status().name();
        e.scheduledAt = s.scheduledAt();
        e.durationMinutes = s.durationMinutes();
        e.interviewerName = s.interviewerName();
        e.interviewRound = s.interviewRound();
        e.location = s.location();
        e.meetingLink = s.meetingLink();
        e.notes = s.notes();
        e.feedback = s.feedback();
        e.rating = s.rating();
        e.questions = toJson(s.questions());
        e.deleted = s.isDeleted();
        e.deletedAt = s.deletedAt();
        return e;
    }

    public InterviewSession toDomain() {
        return InterviewSession.reconstitute(InterviewSessionId.from(id), userId, companyId, jobId,
            type, InterviewStatus.valueOf(status), scheduledAt, durationMinutes, interviewerName,
            interviewRound, location, meetingLink, notes, feedback, rating,
            fromJsonListQuestions(questions), deleted, deletedAt, createdAt, updatedAt);
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : "[]"; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }

    @SuppressWarnings("unchecked")
    private static List<InterviewQuestion> fromJsonListQuestions(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            var rawList = MAPPER.readValue(json, List.class);
            return rawList.stream().map(o -> {
                var map = (java.util.Map<String, Object>) o;
                return new InterviewQuestion(
                    (String) map.get("question"),
                    (String) map.get("expectedAnswer"),
                    map.get("difficulty") instanceof Number n ? n.intValue() : 1,
                    (String) map.get("category"),
                    (java.util.Map<String, Object>) map.get("metadata")
                );
            }).toList();
        } catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
}
