package com.jobpilot.infrastructure.persistence.notification;

import com.jobpilot.application.notification.ports.NotificationRepository;
import com.jobpilot.domain.notification.Notification;
import com.jobpilot.domain.notification.NotificationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryImpl(NotificationJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(NotificationEntity.fromDomain(notification)).toDomain();
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.value()).map(NotificationEntity::toDomain);
    }

    @Override
    public Page<Notification> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(NotificationEntity::toDomain);
    }

    @Override
    public Page<Notification> findByUserIdAndStatus(UUID userId, String status, Pageable pageable) {
        return jpaRepository.findByUserIdAndStatus(userId, status, pageable).map(NotificationEntity::toDomain);
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndStatus(userId, "PENDING");
    }

    @Override
    public void markAllReadByUserId(UUID userId) {
        jpaRepository.markAllReadByUserId(userId, java.time.Instant.now());
    }
}
