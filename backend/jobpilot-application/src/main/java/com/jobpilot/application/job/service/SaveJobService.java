package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.dto.SaveJobCommand;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.job.ports.SavedJobRepository;
import com.jobpilot.application.job.usecase.SaveJobUseCase;
import com.jobpilot.domain.job.JobId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SaveJobService implements SaveJobUseCase {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;

    public SaveJobService(SavedJobRepository savedJobRepository, JobRepository jobRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public JobResponse execute(SaveJobCommand command) {
        var userId = UUID.fromString(command.userId());
        var jobId = JobId.from(UUID.fromString(command.jobId()));
        savedJobRepository.save(userId, jobId, command.notes());
        var job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + command.jobId()));
        return CreateJobService.toResponse(job);
    }
}
