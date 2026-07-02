package com.jobpilot.application.notification.service;

import com.jobpilot.application.notification.ports.EmailSenderPort;
import com.jobpilot.application.notification.ports.NotificationRepository;
import com.jobpilot.application.notification.ports.NotificationSender;
import com.jobpilot.domain.notification.Notification;
import com.jobpilot.domain.notification.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailNotificationService implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailSenderPort emailSender;
    private final NotificationRepository notificationRepository;

    public EmailNotificationService(EmailSenderPort emailSender, NotificationRepository notificationRepository) {
        this.emailSender = emailSender;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void send(Notification notification) {
        if (notification.channel() != NotificationChannel.EMAIL) {
            return;
        }

        try {
            var metadata = notification.metadata();
            String to = null;
            if (metadata != null) {
                var emailObj = metadata.get("email");
                if (emailObj instanceof String) {
                    to = (String) emailObj;
                }
            }

            if (to == null || to.isBlank()) {
                log.warn("No email address in notification metadata for notification {}", notification.notificationId().value());
                notification.markFailed();
                notificationRepository.save(notification);
                return;
            }

            emailSender.send(to, notification.title(), notification.body());
            notification.markSent();
            notification.markDelivered();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to send email notification {}: {}", notification.notificationId().value(), e.getMessage());
            notification.markFailed();
            notificationRepository.save(notification);
        }
    }
}
