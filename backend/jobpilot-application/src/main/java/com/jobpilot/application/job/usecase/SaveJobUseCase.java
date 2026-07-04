package com.jobpilot.application.job.usecase;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.dto.SaveJobCommand;
import com.jobpilot.application.shared.UseCase;

public interface SaveJobUseCase extends UseCase<SaveJobCommand, JobResponse> {}
