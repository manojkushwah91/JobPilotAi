package com.jobpilot.application.automation.usecase;

import com.jobpilot.application.automation.dto.ScheduleNotificationCommand;
import com.jobpilot.application.automation.dto.ScheduledTaskResponse;
import com.jobpilot.application.shared.UseCase;

public interface ScheduleNotificationUseCase extends UseCase<ScheduleNotificationCommand, ScheduledTaskResponse> {}
