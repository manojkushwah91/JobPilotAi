package com.jobpilot.domain.identity.events;

import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.Role;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
    UUID eventId,
    Instant occurredAt,
    UserId userId,
    Email email,
    Role role
) implements DomainEvent {

    public UserRegisteredEvent(UserId userId, Email email, Role role) {
        this(UUID.randomUUID(), Instant.now(), userId, email, role);
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
        return "user.registered";
    }
}
