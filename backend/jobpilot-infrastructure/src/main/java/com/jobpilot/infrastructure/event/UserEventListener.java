package com.jobpilot.infrastructure.event;

import com.jobpilot.application.notification.ports.NotificationRepository;
import com.jobpilot.domain.identity.events.UserRegisteredEvent;
import com.jobpilot.domain.identity.events.UserVerifiedEvent;
import com.jobpilot.domain.notification.Notification;
import com.jobpilot.domain.notification.NotificationChannel;
import com.jobpilot.domain.notification.NotificationId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventListener {

    private final NotificationRepository notificationRepository;

    public UserEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        var welcome = Notification.create(
            NotificationId.generate(),
            event.userId().value(),
            "welcome",
            NotificationChannel.IN_APP,
            "Welcome to JobPilot!",
            "Thank you for registering. Start exploring jobs and opportunities."
        );
        notificationRepository.save(welcome);

        var emailVerification = Notification.create(
            NotificationId.generate(),
            event.userId().value(),
            "email_verification",
            NotificationChannel.EMAIL,
            "Verify your email address",
            "Please verify your email address to activate your account."
        );
        notificationRepository.save(emailVerification);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserVerified(UserVerifiedEvent event) {
        var notification = Notification.create(
            NotificationId.generate(),
            event.userId().value(),
            "welcome",
            NotificationChannel.IN_APP,
            "Email Verified",
            "Your email has been successfully verified."
        );
        notificationRepository.save(notification);
    }
}
