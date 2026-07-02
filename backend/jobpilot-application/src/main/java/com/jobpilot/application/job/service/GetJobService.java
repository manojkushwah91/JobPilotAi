package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.GetJobCommand;
import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.job.usecase.GetJobUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.job.JobId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetJobService implements GetJobUseCase {

    private final JobRepository jobRepository;

    public GetJobService(JobRepository jobRepository) { this.jobRepository = jobRepository; }

    @Override
    public JobResponse execute(GetJobCommand command) {
        var jobId = JobId.from(UUID.fromString(command.jobId()));
        var job = jobRepository.findById(jobId)
            .orElseThrow(() -> new NotFoundException("JobListing", command.jobId()));
        return CreateJobService.toResponse(job);
    }
}
