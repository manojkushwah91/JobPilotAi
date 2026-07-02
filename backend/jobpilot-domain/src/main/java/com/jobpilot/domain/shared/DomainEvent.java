package com.jobpilot.domain.shared;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    String aggregateType();
    UUID aggregateId();
    String eventType();
}
