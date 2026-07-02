package com.jobpilot.application.interview.dto;

import com.jobpilot.domain.interview.InterviewQuestion;
import com.jobpilot.domain.interview.InterviewSession;
import com.jobpilot.domain.interview.InterviewStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InterviewResponse(
    UUID sessionId, UUID userId, UUID companyId, UUID jobId,
    String type, InterviewStatus status, Instant scheduledAt,
    Integer durationMinutes, String interviewerName, Integer interviewRound,
    String location, String meetingLink, String notes,
    String feedback, Integer rating, List<InterviewQuestion> questions,
    Instant createdAt, Instant updatedAt
) {
    public static InterviewResponse from(InterviewSession s) {
        return new InterviewResponse(s.sessionId().value(), s.userId(), s.companyId(), s.jobId(),
            s.type(), s.status(), s.scheduledAt(), s.durationMinutes(), s.interviewerName(),
            s.interviewRound(), s.location(), s.meetingLink(), s.notes(), s.feedback(), s.rating(),
            s.questions(), s.createdAt(), s.updatedAt());
    }
}
