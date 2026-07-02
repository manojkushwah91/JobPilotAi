package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.CreateJobCommand;
import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.job.usecase.CreateJobUseCase;
import com.jobpilot.domain.job.EmploymentType;
import com.jobpilot.domain.job.ExperienceLevel;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateJobService implements CreateJobUseCase {

    private final JobRepository jobRepository;

    public CreateJobService(JobRepository jobRepository) { this.jobRepository = jobRepository; }

    @Override
    public JobResponse execute(CreateJobCommand command) {
        var jobId = JobId.generate();
        var job = JobListing.create(jobId, "manual", command.title(), command.companyName(), command.description());

        if (command.location() != null) job.updateDetails(command.title(), command.description(), command.companyName(),
            command.location(), command.salary(),
            command.employmentType() != null ? EmploymentType.valueOf(command.employmentType()) : null,
            command.experienceLevel() != null ? ExperienceLevel.valueOf(command.experienceLevel()) : null,
            command.industry(), command.skills(), command.applicationUrl());

        jobRepository.save(job);
        return toResponse(job);
    }

    public static JobResponse toResponse(JobListing job) {
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
