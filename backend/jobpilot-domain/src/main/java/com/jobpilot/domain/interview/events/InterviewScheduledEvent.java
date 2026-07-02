package com.jobpilot.domain.interview.events;

import com.jobpilot.domain.interview.InterviewSessionId;
import com.jobpilot.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InterviewScheduledEvent(
    UUID eventId,
    Instant occurredAt,
    InterviewSessionId sessionId,
    UUID userId,
    UUID companyId,
    Instant scheduledAt
) implements DomainEvent {

    public InterviewScheduledEvent(InterviewSessionId sessionId, UUID userId, UUID companyId, Instant scheduledAt) {
        this(UUID.randomUUID(), Instant.now(), sessionId, userId, companyId, scheduledAt);
    }

    @Override public String aggregateType() { return "InterviewSession"; }
    @Override public UUID aggregateId() { return sessionId.value(); }
    @Override public String eventType() { return "interview.scheduled"; }
}
