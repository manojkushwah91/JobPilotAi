package com.jobpilot.domain.billing;

import com.jobpilot.domain.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record BillingEvent(UUID subscriptionId, UUID userId, String eventType) implements DomainEvent {
    @Override public UUID eventId() { return UUID.randomUUID(); }
    @Override public Instant occurredAt() { return Instant.now(); }
    @Override public String aggregateType() { return "Subscription"; }
    @Override public UUID aggregateId() { return subscriptionId; }
}
