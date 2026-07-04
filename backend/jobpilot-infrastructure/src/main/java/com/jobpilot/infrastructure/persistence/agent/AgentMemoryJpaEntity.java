package com.jobpilot.infrastructure.persistence.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_memories")
public class AgentMemoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "memory_type", nullable = false)
    private com.jobpilot.domain.agent.MemoryType memoryType;

    @Column(name = "memory_key", nullable = false)
    private String key;

    @Column(columnDefinition = "text")
    private String value;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    private double confidence;

    @Column(name = "access_count")
    private int accessCount;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AgentMemoryJpaEntity() {}

    public static AgentMemoryJpaEntity fromDomain(com.jobpilot.domain.agent.AgentMemory memory) {
        var entity = new AgentMemoryJpaEntity();
        entity.id = memory.memoryId().value();
        entity.userId = memory.userId();
        entity.memoryType = memory.memoryType();
        entity.key = memory.memoryKey();
        entity.value = memory.value();
        entity.confidence = memory.confidence();
        entity.accessCount = memory.accessCount();
        entity.lastAccessedAt = memory.lastAccessedAt();
        entity.active = memory.isActive();
        entity.createdAt = memory.createdAt();
        entity.updatedAt = memory.updatedAt();
        return entity;
    }

    public com.jobpilot.domain.agent.AgentMemory toDomain() {
        return com.jobpilot.domain.agent.AgentMemory.reconstitute(
            com.jobpilot.domain.agent.MemoryId.from(id),
            userId,
            com.jobpilot.domain.agent.MemoryType.valueOf(memoryType.name()),
            key, value, null,
            confidence, accessCount, lastAccessedAt,
            active, createdAt, updatedAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public com.jobpilot.domain.agent.MemoryType getMemoryType() { return memoryType; }
    public void setMemoryType(com.jobpilot.domain.agent.MemoryType memoryType) { this.memoryType = memoryType; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
