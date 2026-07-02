package com.jobpilot.domain.interview.events;

import com.jobpilot.domain.interview.InterviewSessionId;
import com.jobpilot.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InterviewCompletedEvent(
    UUID eventId,
    Instant occurredAt,
    InterviewSessionId sessionId,
    int rating,
    String feedback
) implements DomainEvent {

    public InterviewCompletedEvent(InterviewSessionId sessionId, int rating, String feedback) {
        this(UUID.randomUUID(), Instant.now(), sessionId, rating, feedback);
    }

    @Override public String aggregateType() { return "InterviewSession"; }
    @Override public UUID aggregateId() { return sessionId.value(); }
    @Override public String eventType() { return "interview.completed"; }
}
