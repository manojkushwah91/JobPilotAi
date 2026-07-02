package com.jobpilot.domain.resume.events;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.resume.ResumeId;
import com.jobpilot.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ResumeCreatedEvent(
    UUID eventId,
    Instant occurredAt,
    ResumeId resumeId,
    UserId userId
) implements DomainEvent {

    public ResumeCreatedEvent(ResumeId resumeId, UserId userId) {
        this(UUID.randomUUID(), Instant.now(), resumeId, userId);
    }

    @Override
    public String aggregateType() {
        return "Resume";
    }

    @Override
    public UUID aggregateId() {
        return resumeId.value();
    }

    @Override
    public String eventType() {
        return "resume.created";
    }
}
