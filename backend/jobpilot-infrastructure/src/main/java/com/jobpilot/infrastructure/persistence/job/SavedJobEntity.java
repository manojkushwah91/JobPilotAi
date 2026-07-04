package com.jobpilot.infrastructure.persistence.job;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saved_jobs")
@IdClass(SavedJobId.class)
public class SavedJobEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "job_listing_id")
    private UUID jobListingId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private Instant savedAt;

    protected SavedJobEntity() {}

    public SavedJobEntity(UUID userId, UUID jobListingId, String notes) {
        this.userId = userId;
        this.jobListingId = jobListingId;
        this.notes = notes;
        this.savedAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public UUID getJobListingId() { return jobListingId; }
    public String getNotes() { return notes; }
    public Instant getSavedAt() { return savedAt; }
}
