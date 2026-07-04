package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AgentMemory extends BaseAggregateRoot {

    private MemoryId memoryId;
    private UUID userId;
    private MemoryType memoryType;
    private String key;
    private String value;
    private Map<String, Object> metadata;
    private double confidence;
    private int accessCount;
    private Instant lastAccessedAt;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private AgentMemory(MemoryId memoryId, UUID userId, MemoryType memoryType, String key, String value) {
        super(memoryId.value());
        this.memoryId = memoryId;
        this.userId = userId;
        this.memoryType = memoryType;
        this.key = key;
        this.value = value;
        this.confidence = 1.0;
        this.accessCount = 0;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static AgentMemory create(UUID userId, MemoryType memoryType, String key, String value) {
        return new AgentMemory(MemoryId.generate(), userId, memoryType, key, value);
    }

    public static AgentMemory reconstitute(MemoryId memoryId, UUID userId, MemoryType memoryType,
                                            String key, String value, Map<String, Object> metadata,
                                            double confidence, int accessCount, Instant lastAccessedAt,
                                            boolean active, Instant createdAt, Instant updatedAt) {
        var m = new AgentMemory(memoryId, userId, memoryType, key, value);
        m.metadata = metadata;
        m.confidence = confidence;
        m.accessCount = accessCount;
        m.lastAccessedAt = lastAccessedAt;
        m.active = active;
        m.createdAt = createdAt;
        m.updatedAt = updatedAt;
        return m;
    }

    public void access() {
        this.accessCount++;
        this.lastAccessedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateValue(String newValue) {
        this.value = newValue;
        this.updatedAt = Instant.now();
    }

    public void updateConfidence(double newConfidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, newConfidence));
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public MemoryId memoryId() { return memoryId; }
    public UUID userId() { return userId; }
    public MemoryType memoryType() { return memoryType; }
    public String key() { return key; }
    public String value() { return value; }
    public Map<String, Object> metadata() { return metadata; }
    public double confidence() { return confidence; }
    public int accessCount() { return accessCount; }
    public Instant lastAccessedAt() { return lastAccessedAt; }
    public boolean isActive() { return active; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
