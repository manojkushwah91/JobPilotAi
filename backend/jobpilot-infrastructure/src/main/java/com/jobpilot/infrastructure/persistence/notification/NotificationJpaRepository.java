package com.jobpilot.infrastructure.persistence.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    Page<NotificationEntity> findByUserId(UUID userId, Pageable pageable);
    Page<NotificationEntity> findByUserIdAndStatus(UUID userId, String status, Pageable pageable);
    long countByUserIdAndStatus(UUID userId, String status);
}
