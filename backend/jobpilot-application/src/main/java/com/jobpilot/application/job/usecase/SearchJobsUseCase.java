package com.jobpilot.application.job.usecase;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.dto.SearchJobsCommand;
import com.jobpilot.application.shared.UseCase;
import org.springframework.data.domain.Page;

public interface SearchJobsUseCase extends UseCase<SearchJobsCommand, Page<JobResponse>> {}
