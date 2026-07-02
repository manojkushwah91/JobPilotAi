package com.jobpilot.application.application.usecase;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.UpdateApplicationStatusCommand;
import com.jobpilot.application.shared.UseCase;

public interface UpdateApplicationStatusUseCase extends UseCase<UpdateApplicationStatusCommand, ApplicationResponse> {}
