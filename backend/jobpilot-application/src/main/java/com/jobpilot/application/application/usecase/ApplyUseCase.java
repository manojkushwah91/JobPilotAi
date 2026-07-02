package com.jobpilot.application.application.usecase;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.ApplyCommand;
import com.jobpilot.application.shared.UseCase;

public interface ApplyUseCase extends UseCase<ApplyCommand, ApplicationResponse> {}
