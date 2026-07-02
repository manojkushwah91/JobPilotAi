package com.jobpilot.application.notification.service;

import com.jobpilot.application.notification.dto.MarkAsReadCommand;
import com.jobpilot.application.notification.ports.NotificationRepository;
import com.jobpilot.application.notification.usecase.MarkAsReadUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.notification.NotificationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MarkAsReadService implements MarkAsReadUseCase {

    private final NotificationRepository notificationRepository;

    public MarkAsReadService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Void execute(MarkAsReadCommand command) {
        var id = NotificationId.from(UUID.fromString(command.notificationId()));
        var notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Notification", command.notificationId()));
        notification.markRead();
        notificationRepository.save(notification);
        return null;
    }
}
