package com.jobpilot.infrastructure.persistence.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    Page<NotificationEntity> findByUserId(UUID userId, Pageable pageable);
    Page<NotificationEntity> findByUserIdAndStatus(UUID userId, String status, Pageable pageable);
    long countByUserIdAndStatus(UUID userId, String status);

    @Modifying
    @Query("update NotificationEntity n set n.status = 'READ', n.readAt = :now where n.userId = :userId and n.status <> 'READ'")
    int markAllReadByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
