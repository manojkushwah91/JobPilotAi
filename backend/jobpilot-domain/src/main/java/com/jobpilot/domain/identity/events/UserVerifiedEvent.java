package com.jobpilot.domain.identity.events;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserVerifiedEvent(
    UUID eventId,
    Instant occurredAt,
    UserId userId
) implements DomainEvent {

    public UserVerifiedEvent(UserId userId) {
        this(UUID.randomUUID(), Instant.now(), userId);
    }

    @Override
    public String aggregateType() {
        return "User";
    }

    @Override
    public UUID aggregateId() {
        return userId.value();
    }

    @Override
    public String eventType() {
        return "user.verified";
    }
}
