package com.jobpilot.application.automation.usecase;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.ConfirmActionCommand;
import com.jobpilot.application.shared.UseCase;

public interface ConfirmAutomationUseCase extends UseCase<ConfirmActionCommand, AutomationSessionResponse> {}
