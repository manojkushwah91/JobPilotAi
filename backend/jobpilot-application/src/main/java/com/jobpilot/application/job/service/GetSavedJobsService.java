package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.job.ports.SavedJobRepository;
import com.jobpilot.application.job.usecase.GetSavedJobsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetSavedJobsService implements GetSavedJobsUseCase {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;

    public GetSavedJobsService(SavedJobRepository savedJobRepository, JobRepository jobRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public List<JobResponse> execute(String userId) {
        var savedJobIds = savedJobRepository.findAllByUserId(UUID.fromString(userId));
        return savedJobIds.stream()
            .map(jobRepository::findById)
            .flatMap(opt -> opt.map(job -> List.of(CreateJobService.toResponse(job))).orElse(List.of()).stream())
            .toList();
    }
}
