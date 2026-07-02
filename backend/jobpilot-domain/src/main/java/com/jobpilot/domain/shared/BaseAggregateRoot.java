package com.jobpilot.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class BaseAggregateRoot extends BaseEntity {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected BaseAggregateRoot() {
        super();
    }

    protected BaseAggregateRoot(UUID id) {
        super(id);
    }

    protected BaseAggregateRoot(UUID id, long version) {
        super(id, version);
    }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> drainEvents() {
        if (domainEvents.isEmpty()) return List.of();
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public List<DomainEvent> peekEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
