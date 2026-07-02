package com.jobpilot.domain.shared;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    @Test
    void shouldImplementDomainEventContract() {
        var aggregateId = UUID.randomUUID();
        var event = new TestEvent(aggregateId);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.aggregateType()).isEqualTo("Test");
        assertThat(event.aggregateId()).isEqualTo(aggregateId);
        assertThat(event.eventType()).isEqualTo("TEST");
    }

    record TestEvent(UUID aggregateId) implements DomainEvent {
        @Override
        public UUID eventId() { return UUID.randomUUID(); }

        @Override
        public Instant occurredAt() { return Instant.now(); }

        @Override
        public String aggregateType() { return "Test"; }

        @Override
        public UUID aggregateId() { return aggregateId; }

        @Override
        public String eventType() { return "TEST"; }
    }
}
