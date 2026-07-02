package com.jobpilot.domain.shared;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class BaseAggregateRootTest {

    @Test
    void shouldRegisterAndDrainEvents() {
        var aggregate = new TestAggregate();
        var event = new TestDomainEvent(aggregate.id());

        aggregate.apply(event);

        var events = aggregate.drainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(event);
    }

    @Test
    void shouldClearEventsAfterDrain() {
        var aggregate = new TestAggregate();
        aggregate.apply(new TestDomainEvent(aggregate.id()));

        aggregate.drainEvents();
        var afterDrain = aggregate.drainEvents();
        assertThat(afterDrain).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoEvents() {
        var aggregate = new TestAggregate();
        assertThat(aggregate.drainEvents()).isEmpty();
    }

    @Test
    void shouldPeekWithoutClearing() {
        var aggregate = new TestAggregate();
        var event = new TestDomainEvent(aggregate.id());
        aggregate.apply(event);

        assertThat(aggregate.peekEvents()).hasSize(1);
        assertThat(aggregate.drainEvents()).hasSize(1);
    }

    @Test
    void shouldReturnUnmodifiablePeek() {
        var aggregate = new TestAggregate();
        var events = aggregate.peekEvents();
        assertThat(events).isEmpty();
    }

    static class TestAggregate extends BaseAggregateRoot {
        TestAggregate() {
            super();
        }

        void apply(DomainEvent event) {
            registerEvent(event);
        }
    }

    record TestDomainEvent(UUID aggregateId) implements DomainEvent {
        @Override
        public UUID eventId() { return UUID.randomUUID(); }

        @Override
        public Instant occurredAt() { return Instant.now(); }

        @Override
        public String aggregateType() { return "TestAggregate"; }

        @Override
        public UUID aggregateId() { return aggregateId; }

        @Override
        public String eventType() { return "TEST_EVENT"; }
    }
}
