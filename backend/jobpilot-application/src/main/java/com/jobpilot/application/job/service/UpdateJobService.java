package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.dto.UpdateJobCommand;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.job.usecase.UpdateJobUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.job.EmploymentType;
import com.jobpilot.domain.job.ExperienceLevel;
import com.jobpilot.domain.job.JobId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateJobService implements UpdateJobUseCase {

    private final JobRepository jobRepository;

    public UpdateJobService(JobRepository jobRepository) { this.jobRepository = jobRepository; }

    @Override
    public JobResponse execute(UpdateJobCommand command) {
        var jobId = JobId.from(UUID.fromString(command.jobId()));
        var job = jobRepository.findById(jobId)
            .orElseThrow(() -> new NotFoundException("JobListing", command.jobId()));

        job.updateDetails(command.title(), command.description(), command.companyName(),
            command.location(), command.salary(),
            command.employmentType() != null ? EmploymentType.valueOf(command.employmentType()) : null,
            command.experienceLevel() != null ? ExperienceLevel.valueOf(command.experienceLevel()) : null,
            command.industry(), command.skills(), command.applicationUrl());

        jobRepository.save(job);
        return CreateJobService.toResponse(job);
    }
}
