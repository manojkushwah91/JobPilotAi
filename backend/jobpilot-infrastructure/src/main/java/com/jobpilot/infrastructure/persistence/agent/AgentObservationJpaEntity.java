package com.jobpilot.infrastructure.persistence.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_observations")
public class AgentObservationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mission_id", nullable = false)
    private UUID missionId;

    @Column(name = "task_id")
    private UUID taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "observation_type", nullable = false)
    private com.jobpilot.domain.agent.ObservationType observationType;

    private String source;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "jsonb")
    private String data;

    @Column(name = "relevance_score")
    private double relevanceScore;

    private boolean actionable;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AgentObservationJpaEntity() {}

    public static AgentObservationJpaEntity fromDomain(com.jobpilot.domain.agent.AgentObservation obs) {
        var entity = new AgentObservationJpaEntity();
        entity.id = obs.observationId().value();
        entity.missionId = obs.missionId();
        entity.taskId = obs.taskId();
        entity.observationType = obs.type();
        entity.source = obs.source();
        entity.description = obs.description();
        entity.relevanceScore = obs.relevanceScore();
        entity.actionable = obs.isActionable();
        entity.createdAt = obs.createdAt();
        return entity;
    }

    public com.jobpilot.domain.agent.AgentObservation toDomain() {
        return com.jobpilot.domain.agent.AgentObservation.reconstitute(
            com.jobpilot.domain.agent.ObservationId.from(id),
            missionId, taskId,
            com.jobpilot.domain.agent.ObservationType.valueOf(observationType.name()),
            source, description, null,
            relevanceScore, actionable, createdAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMissionId() { return missionId; }
    public void setMissionId(UUID missionId) { this.missionId = missionId; }
    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public com.jobpilot.domain.agent.ObservationType getObservationType() { return observationType; }
    public void setObservationType(com.jobpilot.domain.agent.ObservationType observationType) { this.observationType = observationType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; }
    public boolean isActionable() { return actionable; }
    public void setActionable(boolean actionable) { this.actionable = actionable; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
