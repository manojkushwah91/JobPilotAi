package com.jobpilot.application.job.usecase;

import com.jobpilot.application.job.dto.GetJobCommand;
import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.shared.UseCase;

public interface GetJobUseCase extends UseCase<GetJobCommand, JobResponse> {}
