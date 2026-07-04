package com.jobpilot.application.job.dto;

public record SaveJobCommand(String userId, String jobId, String notes) {
    public SaveJobCommand {
        if (userId == null || userId.isBlank()) userId = null;
        if (jobId == null || jobId.isBlank()) throw new IllegalArgumentException("jobId is required");
    }
}
