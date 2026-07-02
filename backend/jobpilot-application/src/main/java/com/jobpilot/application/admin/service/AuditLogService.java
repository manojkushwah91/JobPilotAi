package com.jobpilot.application.admin.service;

import com.jobpilot.application.admin.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogPersistencePort persistencePort;

    public AuditLogService(AuditLogPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    public void log(UUID actorId, String actorEmail, String action, String resourceType,
                    String resourceId, Map<String, Object> details) {
        persistencePort.save(actorId, actorEmail, action, resourceType, resourceId, details);
    }

    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return persistencePort.findAll(pageable);
    }
}
