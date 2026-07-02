package com.jobpilot.infrastructure.persistence.admin;

import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "actor_id", nullable = false) private UUID actorId;
    @Column(name = "actor_email") private String actorEmail;
    @Column(name = "action", nullable = false) private String action;
    @Column(name = "resource_type") private String resourceType;
    @Column(name = "resource_id") private String resourceId;
    @Column(name = "details", columnDefinition = "jsonb") private String details;
    @Column(name = "ip_address") private String ipAddress;
    @Column(name = "user_agent") private String userAgent;

    protected AuditLogEntity() {}

    public AuditLogEntity(UUID id, UUID actorId, String actorEmail, String action,
                          String resourceType, String resourceId, String details,
                          String ipAddress, String userAgent) {
        this.id = id;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public UUID getId() { return id; }
    public UUID getActorId() { return actorId; }
    public String getActorEmail() { return actorEmail; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Instant getCreatedAt() { return createdAt; }
}
