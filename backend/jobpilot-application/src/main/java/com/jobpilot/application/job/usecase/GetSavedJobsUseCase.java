package com.jobpilot.application.job.usecase;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.shared.UseCase;
import java.util.List;

public interface GetSavedJobsUseCase extends UseCase<String, List<JobResponse>> {}
