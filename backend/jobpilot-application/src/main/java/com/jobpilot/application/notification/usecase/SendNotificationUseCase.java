package com.jobpilot.application.notification.usecase;

import com.jobpilot.application.notification.dto.NotificationResponse;
import com.jobpilot.application.notification.dto.SendNotificationCommand;
import com.jobpilot.application.shared.UseCase;

public interface SendNotificationUseCase extends UseCase<SendNotificationCommand, NotificationResponse> {}
