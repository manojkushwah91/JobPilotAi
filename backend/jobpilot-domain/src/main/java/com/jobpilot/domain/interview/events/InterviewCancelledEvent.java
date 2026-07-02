package com.jobpilot.domain.interview.events;

import com.jobpilot.domain.interview.InterviewSessionId;
import com.jobpilot.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InterviewCancelledEvent(
    UUID eventId,
    Instant occurredAt,
    InterviewSessionId sessionId,
    String reason
) implements DomainEvent {

    public InterviewCancelledEvent(InterviewSessionId sessionId, String reason) {
        this(UUID.randomUUID(), Instant.now(), sessionId, reason);
    }

    @Override public String aggregateType() { return "InterviewSession"; }
    @Override public UUID aggregateId() { return sessionId.value(); }
    @Override public String eventType() { return "interview.cancelled"; }
}
