package com.jobpilot.infrastructure.persistence.shared;

import com.jobpilot.common.util.TimeProvider;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@MappedSuperclass
public abstract class BaseJpaEntity {

    private static final TimeProvider DEFAULT_TIME = TimeProvider.system();

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = DEFAULT_TIME.now();
        updatedAt = DEFAULT_TIME.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = DEFAULT_TIME.now();
    }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
