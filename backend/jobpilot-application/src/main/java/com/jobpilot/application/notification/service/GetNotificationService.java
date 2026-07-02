package com.jobpilot.application.notification.service;

import com.jobpilot.application.notification.dto.NotificationResponse;
import com.jobpilot.application.notification.dto.GetNotificationCommand;
import com.jobpilot.application.notification.ports.NotificationRepository;
import com.jobpilot.application.notification.usecase.GetNotificationUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.notification.NotificationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetNotificationService implements GetNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public GetNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationResponse execute(GetNotificationCommand command) {
        var id = NotificationId.from(UUID.fromString(command.notificationId()));
        var notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Notification", command.notificationId()));
        return NotificationResponse.from(notification);
    }
}
