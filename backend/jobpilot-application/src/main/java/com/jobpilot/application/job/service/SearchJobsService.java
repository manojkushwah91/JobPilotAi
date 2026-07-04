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
        var hasFilters = command.query() != null && !command.query().isBlank()
            || (command.skills() != null && !command.skills().isEmpty())
            || command.employmentType() != null && !command.employmentType().isBlank()
            || command.experienceLevel() != null && !command.experienceLevel().isBlank()
            || command.industry() != null && !command.industry().isBlank()
            || command.location() != null && !command.location().isBlank()
            || command.salaryMin() != null || command.salaryMax() != null
            || command.postedWithin() != null && !command.postedWithin().isBlank();

        if (hasFilters) {
            return jobRepository.searchFiltered(
                command.query(), command.skills(), command.employmentType(),
                command.experienceLevel(), command.industry(), command.location(),
                command.salaryMin(), command.salaryMax(), command.postedWithin(),
                pageable
            ).map(CreateJobService::toResponse);
        }
        return jobRepository.findAllActive(pageable)
            .map(CreateJobService::toResponse);
    }
}
