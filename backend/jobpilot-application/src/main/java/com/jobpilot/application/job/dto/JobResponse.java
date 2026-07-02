package com.jobpilot.application.job.dto;

import com.jobpilot.domain.job.JobListing;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record JobResponse(
    String id,
    String source,
    String sourceId,
    String title,
    String companyName,
    String companyLogoUrl,
    String companyId,
    Map<String, Object> location,
    Map<String, Object> salary,
    String description,
    List<String> requirements,
    List<String> responsibilities,
    List<String> benefits,
    String employmentType,
    String experienceLevel,
    String industry,
    List<String> skills,
    String applicationUrl,
    Instant postedAt,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    public static JobResponse from(JobListing job) {
        return new JobResponse(
            job.jobId().value().toString(), job.source(), job.sourceId(),
            job.title(), job.companyName(), job.companyLogoUrl(),
            job.companyId() != null ? job.companyId().value().toString() : null,
            job.location(), job.salary(), job.description(),
            job.requirements(), job.responsibilities(), job.benefits(),
            job.employmentType() != null ? job.employmentType().name() : null,
            job.experienceLevel() != null ? job.experienceLevel().name() : null,
            job.industry(), job.skills(), job.applicationUrl(),
            job.postedAt(), job.isActive(), job.createdAt(), job.updatedAt()
        );
    }
}
