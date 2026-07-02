package com.jobpilot.infrastructure.persistence.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, UUID> {
    List<ApplicationEntity> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    List<ApplicationEntity> findByUserIdAndStatusOrderByUpdatedAtDesc(UUID userId, String status);
}
