package com.jobpilot.application.job.usecase;

import com.jobpilot.application.job.dto.CreateJobCommand;
import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.shared.UseCase;

public interface CreateJobUseCase extends UseCase<CreateJobCommand, JobResponse> {}
