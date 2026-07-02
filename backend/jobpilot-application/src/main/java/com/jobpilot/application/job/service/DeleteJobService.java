package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.DeleteJobCommand;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.job.usecase.DeleteJobUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.job.JobId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteJobService implements DeleteJobUseCase {

    private final JobRepository jobRepository;

    public DeleteJobService(JobRepository jobRepository) { this.jobRepository = jobRepository; }

    @Override
    public Void execute(DeleteJobCommand command) {
        var jobId = JobId.from(UUID.fromString(command.jobId()));
        var job = jobRepository.findById(jobId)
            .orElseThrow(() -> new NotFoundException("JobListing", command.jobId()));
        job.deactivate();
        jobRepository.save(job);
        return null;
    }
}
