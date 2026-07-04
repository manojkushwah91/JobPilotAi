package com.jobpilot.application.job.dto;

import java.time.Instant;

public record SavedJobResponse(
    String userId,
    String jobId,
    String notes,
    Instant savedAt
) {}
