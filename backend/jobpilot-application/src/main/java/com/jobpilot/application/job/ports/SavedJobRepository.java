package com.jobpilot.application.job.ports;

import com.jobpilot.domain.job.JobId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedJobRepository {
    void save(UUID userId, JobId jobId, String notes);
    void delete(UUID userId, JobId jobId);
    boolean isSaved(UUID userId, JobId jobId);
    List<JobId> findAllByUserId(UUID userId);
    Optional<String> getNotes(UUID userId, JobId jobId);
}
