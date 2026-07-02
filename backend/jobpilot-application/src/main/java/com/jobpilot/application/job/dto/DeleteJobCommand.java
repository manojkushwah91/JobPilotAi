package com.jobpilot.application.job.dto;

import com.jobpilot.common.exception.ValidationException;

public record DeleteJobCommand(String jobId) {
    public DeleteJobCommand {
        if (jobId == null || jobId.isBlank()) throw new ValidationException("jobId", "Job ID must not be blank");
    }
}
