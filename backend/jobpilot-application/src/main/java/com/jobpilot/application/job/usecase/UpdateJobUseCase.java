package com.jobpilot.application.job.usecase;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.dto.UpdateJobCommand;
import com.jobpilot.application.shared.UseCase;

public interface UpdateJobUseCase extends UseCase<UpdateJobCommand, JobResponse> {}
