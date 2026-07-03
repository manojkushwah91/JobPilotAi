package com.jobpilot.infrastructure.event;

import com.jobpilot.domain.shared.BaseAggregateRoot;
import com.jobpilot.domain.shared.DomainEvent;
import com.jobpilot.domain.shared.DomainEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class DomainEventPublishingRepositoryDecorator {

    private final DomainEventPublisher eventPublisher;

    public DomainEventPublishingRepositoryDecorator(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public <T extends BaseAggregateRoot> T publishEvents(T aggregate, Supplier<T> saveAction) {
        T saved = saveAction.get();
        List<DomainEvent> events = aggregate.drainEvents();
        if (!events.isEmpty()) {
            eventPublisher.publishAll(events);
        }
        return saved;
    }
}
