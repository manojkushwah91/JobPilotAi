package com.jobpilot.application.admin.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
    UUID actorId, String actorEmail, String action, String resourceType,
    String resourceId, Map<String, Object> details, Instant createdAt
) {}
