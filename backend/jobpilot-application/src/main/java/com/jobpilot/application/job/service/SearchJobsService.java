package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.dto.SearchJobsCommand;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.job.usecase.SearchJobsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchJobsService implements SearchJobsUseCase {

    private final JobRepository jobRepository;

    public SearchJobsService(JobRepository jobRepository) { this.jobRepository = jobRepository; }

    @Override
    public Page<JobResponse> execute(SearchJobsCommand command) {
        var pageable = PageRequest.of(command.page(), command.size());
        if (command.query() != null && !command.query().isBlank()) {
            return jobRepository.search(command.query(), pageable)
                .map(CreateJobService::toResponse);
        }
        return jobRepository.findAllActive(pageable)
            .map(CreateJobService::toResponse);
    }
}
