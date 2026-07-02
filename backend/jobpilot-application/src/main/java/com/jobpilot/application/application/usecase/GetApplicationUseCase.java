package com.jobpilot.application.application.usecase;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.GetApplicationCommand;
import com.jobpilot.application.shared.UseCase;

public interface GetApplicationUseCase extends UseCase<GetApplicationCommand, ApplicationResponse> {}
