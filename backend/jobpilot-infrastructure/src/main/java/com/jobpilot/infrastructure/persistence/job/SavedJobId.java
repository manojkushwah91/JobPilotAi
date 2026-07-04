package com.jobpilot.infrastructure.persistence.job;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SavedJobId implements Serializable {
    private UUID userId;
    private UUID jobListingId;

    public SavedJobId() {}
    public SavedJobId(UUID userId, UUID jobListingId) {
        this.userId = userId;
        this.jobListingId = jobListingId;
    }

    public UUID getUserId() { return userId; }
    public UUID getJobListingId() { return jobListingId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedJobId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(jobListingId, that.jobListingId);
    }

    @Override
    public int hashCode() { return Objects.hash(userId, jobListingId); }
}
