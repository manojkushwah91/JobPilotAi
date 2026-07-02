package com.jobpilot.application.automation.usecase;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.StartAutomationCommand;
import com.jobpilot.application.shared.UseCase;

public interface StartAutomationUseCase extends UseCase<StartAutomationCommand, AutomationSessionResponse> {}
