package com.jobpilot.application.notification.usecase;

import com.jobpilot.application.notification.dto.NotificationResponse;
import com.jobpilot.application.notification.dto.GetNotificationCommand;
import com.jobpilot.application.shared.UseCase;

public interface GetNotificationUseCase extends UseCase<GetNotificationCommand, NotificationResponse> {}
