package com.jobpilot.application.job.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.List;
import java.util.Map;

public record UpdateJobCommand(
    String jobId,
    String title,
    String companyName,
    String description,
    Map<String, Object> location,
    Map<String, Object> salary,
    String employmentType,
    String experienceLevel,
    String industry,
    List<String> skills,
    String applicationUrl
) {
    public UpdateJobCommand {
        if (jobId == null || jobId.isBlank()) throw new ValidationException("jobId", "Job ID must not be blank");
        if (title == null || title.isBlank()) throw new ValidationException("title", "Title must not be blank");
        if (title.length() > 255) throw new ValidationException("title", "Title must not exceed 255 characters");
        if (companyName == null || companyName.isBlank()) throw new ValidationException("companyName", "Company name must not be blank");
        if (companyName.length() > 255) throw new ValidationException("companyName", "Company name must not exceed 255 characters");
        if (description == null || description.isBlank()) throw new ValidationException("description", "Description must not be blank");
    }
}
