package com.jobpilot.domain.interview;

import com.jobpilot.domain.interview.events.InterviewCancelledEvent;
import com.jobpilot.domain.interview.events.InterviewCompletedEvent;
import com.jobpilot.domain.interview.events.InterviewScheduledEvent;
import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InterviewSession extends BaseAggregateRoot {

    private final InterviewSessionId sessionId;
    private UUID userId;
    private UUID companyId;
    private UUID jobId;
    private String type;
    private InterviewStatus status;
    private Instant scheduledAt;
    private Integer durationMinutes;
    private String interviewerName;
    private Integer interviewRound;
    private String location;
    private String meetingLink;
    private String notes;
    private String feedback;
    private Integer rating;
    private final List<InterviewQuestion> questions;
    private boolean deleted;
    private Instant deletedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private InterviewSession(InterviewSessionId sessionId, UUID userId, UUID companyId, UUID jobId,
                              String type, Instant scheduledAt) {
        super(sessionId.value());
        this.sessionId = sessionId;
        this.userId = userId;
        this.companyId = companyId;
        this.jobId = jobId;
        this.type = type;
        this.status = InterviewStatus.SCHEDULED;
        this.scheduledAt = scheduledAt;
        this.questions = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static InterviewSession schedule(InterviewSessionId sessionId, UUID userId, UUID companyId,
                                             UUID jobId, String type, Instant scheduledAt) {
        var session = new InterviewSession(sessionId, userId, companyId, jobId, type, scheduledAt);
        session.registerEvent(new InterviewScheduledEvent(sessionId, userId, companyId, scheduledAt));
        return session;
    }

    public static InterviewSession reconstitute(InterviewSessionId sessionId, UUID userId, UUID companyId,
            UUID jobId, String type, InterviewStatus status, Instant scheduledAt, Integer durationMinutes,
            String interviewerName, Integer interviewRound, String location, String meetingLink,
            String notes, String feedback, Integer rating, List<InterviewQuestion> questions,
            boolean deleted, Instant deletedAt, Instant createdAt, Instant updatedAt) {
        var session = new InterviewSession(sessionId, userId, companyId, jobId, type, scheduledAt);
        session.status = status;
        session.durationMinutes = durationMinutes;
        session.interviewerName = interviewerName;
        session.interviewRound = interviewRound;
        session.location = location;
        session.meetingLink = meetingLink;
        session.notes = notes;
        session.feedback = feedback;
        session.rating = rating;
        session.questions.addAll(questions);
        session.deleted = deleted;
        session.deletedAt = deletedAt;
        return session;
    }

    public void complete(int rating, String feedback) {
        this.status = InterviewStatus.COMPLETED;
        this.rating = rating;
        this.feedback = feedback;
        this.updatedAt = Instant.now();
        registerEvent(new InterviewCompletedEvent(sessionId, rating, feedback));
    }

    public void cancel(String reason) {
        if (status == InterviewStatus.COMPLETED) return;
        this.status = InterviewStatus.CANCELLED;
        this.updatedAt = Instant.now();
        registerEvent(new InterviewCancelledEvent(sessionId, reason));
    }

    public void reschedule(Instant newTime) {
        this.status = InterviewStatus.RESCHEDULED;
        this.scheduledAt = newTime;
        this.updatedAt = Instant.now();
    }

    public void markNoShow() {
        this.status = InterviewStatus.NO_SHOW;
        this.updatedAt = Instant.now();
    }

    public void updateDetails(Integer durationMinutes, String interviewerName, String location,
                               String meetingLink, String notes) {
        this.durationMinutes = durationMinutes;
        this.interviewerName = interviewerName;
        this.location = location;
        this.meetingLink = meetingLink;
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public void addQuestion(InterviewQuestion question) {
        questions.add(question);
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (deleted) return;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public InterviewSessionId sessionId() { return sessionId; }
    public UUID userId() { return userId; }
    public UUID companyId() { return companyId; }
    public UUID jobId() { return jobId; }
    public String type() { return type; }
    public InterviewStatus status() { return status; }
    public Instant scheduledAt() { return scheduledAt; }
    public Integer durationMinutes() { return durationMinutes; }
    public String interviewerName() { return interviewerName; }
    public Integer interviewRound() { return interviewRound; }
    public String location() { return location; }
    public String meetingLink() { return meetingLink; }
    public String notes() { return notes; }
    public String feedback() { return feedback; }
    public Integer rating() { return rating; }
    public List<InterviewQuestion> questions() { return List.copyOf(questions); }
    public boolean isDeleted() { return deleted; }
    public Instant deletedAt() { return deletedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
