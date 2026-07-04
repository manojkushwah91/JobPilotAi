package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.SaveJobCommand;
import com.jobpilot.application.job.ports.SavedJobRepository;
import com.jobpilot.application.job.usecase.UnsaveJobUseCase;
import com.jobpilot.domain.job.JobId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UnsaveJobService implements UnsaveJobUseCase {

    private final SavedJobRepository savedJobRepository;

    public UnsaveJobService(SavedJobRepository savedJobRepository) {
        this.savedJobRepository = savedJobRepository;
    }

    @Override
    public Void execute(SaveJobCommand command) {
        var userId = UUID.fromString(command.userId());
        var jobId = JobId.from(UUID.fromString(command.jobId()));
        savedJobRepository.delete(userId, jobId);
        return null;
    }
}
