package com.jobpilot.infrastructure.persistence.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.application.admin.dto.AuditLogResponse;
import com.jobpilot.application.admin.service.AuditLogPersistencePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class AuditLogPersistenceAdapter implements AuditLogPersistencePort {

    private final AuditLogJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public AuditLogPersistenceAdapter(AuditLogJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(UUID actorId, String actorEmail, String action, String resourceType,
                     String resourceId, Map<String, Object> details) {
        var entity = new AuditLogEntity(
            UUID.randomUUID(), actorId, actorEmail, action, resourceType, resourceId,
            toJson(details), null, null
        );
        jpaRepository.save(entity);
    }

    @Override
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(e -> {
            Map<String, Object> detailsMap = null;
            if (e.getDetails() != null) {
                try { detailsMap = objectMapper.readValue(e.getDetails(), Map.class); }
                catch (Exception ignored) {}
            }
            return new AuditLogResponse(
                e.getActorId(), e.getActorEmail(), e.getAction(),
                e.getResourceType(), e.getResourceId(), detailsMap, e.getCreatedAt()
            );
        });
    }

    private String toJson(Object obj) {
        try { return obj != null ? objectMapper.writeValueAsString(obj) : null; }
        catch (JsonProcessingException e) { throw new RuntimeException("JSON error", e); }
    }
}
