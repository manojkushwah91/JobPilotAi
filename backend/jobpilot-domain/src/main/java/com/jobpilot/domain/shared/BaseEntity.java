package com.jobpilot.domain.shared;

import java.util.Objects;
import java.util.UUID;

public abstract class BaseEntity {
    private final UUID id;
    private long version;

    protected BaseEntity() {
        this(UUID.randomUUID(), 0);
    }

    protected BaseEntity(UUID id) {
        this(id, 0);
    }

    protected BaseEntity(UUID id, long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.version = version;
    }

    public UUID id() {
        return id;
    }

    public long version() {
        return version;
    }

    protected void incrementVersion() {
        version++;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return Objects.equals(id, ((BaseEntity) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", version=" + version + "}";
    }
}
