package com.jobpilot.application.notification.service;

import com.jobpilot.application.notification.dto.NotificationResponse;
import com.jobpilot.application.notification.dto.SendNotificationCommand;
import com.jobpilot.application.notification.ports.NotificationRepository;
import com.jobpilot.application.notification.usecase.SendNotificationUseCase;
import com.jobpilot.domain.notification.Notification;
import com.jobpilot.domain.notification.NotificationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SendNotificationService implements SendNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public SendNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationResponse execute(SendNotificationCommand command) {
        var notificationId = NotificationId.generate();
        var notification = Notification.create(notificationId, command.userId(), command.type(),
            command.channel(), command.title(), command.body());
        if (command.metadata() != null && !command.metadata().isEmpty()) {
            notification = Notification.reconstitute(notificationId, command.userId(), command.type(),
                command.channel(), command.title(), command.body(), command.metadata(),
                notification.status(), notification.readAt(), notification.sentAt(),
                notification.deliveredAt(), notification.createdAt());
        }
        notificationRepository.save(notification);
        return NotificationResponse.from(notification);
    }
}
