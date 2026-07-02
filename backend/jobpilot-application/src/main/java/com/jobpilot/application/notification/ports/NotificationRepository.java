package com.jobpilot.application.notification.ports;

import com.jobpilot.domain.notification.Notification;
import com.jobpilot.domain.notification.NotificationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(NotificationId id);
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
    Page<Notification> findByUserIdAndStatus(UUID userId, String status, Pageable pageable);
    long countUnreadByUserId(UUID userId);
}
