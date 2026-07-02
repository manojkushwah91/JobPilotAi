package com.jobpilot.application.application.usecase;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.ListApplicationsCommand;
import com.jobpilot.application.shared.UseCase;

import java.util.List;

public interface ListApplicationsUseCase extends UseCase<ListApplicationsCommand, List<ApplicationResponse>> {}
