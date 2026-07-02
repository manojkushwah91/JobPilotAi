package com.jobpilot.application.job.dto;

import com.jobpilot.common.exception.ValidationException;

public record GetJobCommand(String jobId) {
    public GetJobCommand {
        if (jobId == null || jobId.isBlank()) throw new ValidationException("jobId", "Job ID must not be blank");
    }
}
