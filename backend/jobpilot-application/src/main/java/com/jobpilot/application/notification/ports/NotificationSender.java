package com.jobpilot.application.notification.ports;

import com.jobpilot.domain.notification.Notification;

public interface NotificationSender {
    void send(Notification notification);
}
