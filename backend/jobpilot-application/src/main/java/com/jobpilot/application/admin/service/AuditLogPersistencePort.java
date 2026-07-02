package com.jobpilot.application.admin.service;

import com.jobpilot.application.admin.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface AuditLogPersistencePort {
    void save(UUID actorId, String actorEmail, String action, String resourceType,
              String resourceId, Map<String, Object> details);
    Page<AuditLogResponse> findAll(Pageable pageable);
}
