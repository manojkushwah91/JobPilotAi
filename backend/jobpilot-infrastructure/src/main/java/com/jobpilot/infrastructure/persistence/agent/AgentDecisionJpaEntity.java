package com.jobpilot.infrastructure.persistence.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_decisions")
public class AgentDecisionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mission_id", nullable = false)
    private UUID missionId;

    @Column(name = "task_id")
    private UUID taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false)
    private com.jobpilot.domain.agent.DecisionType decisionType;

    @Column(columnDefinition = "text")
    private String reasoning;

    @Column(columnDefinition = "jsonb")
    private String context;

    @Column(columnDefinition = "jsonb")
    private String decision;

    private double confidence;

    private boolean executed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AgentDecisionJpaEntity() {}

    public static AgentDecisionJpaEntity fromDomain(com.jobpilot.domain.agent.AgentDecision dec) {
        var entity = new AgentDecisionJpaEntity();
        entity.id = dec.decisionId().value();
        entity.missionId = dec.missionId();
        entity.taskId = dec.taskId();
        entity.decisionType = dec.type();
        entity.reasoning = dec.reasoning();
        entity.confidence = dec.confidence();
        entity.executed = dec.isExecuted();
        entity.createdAt = dec.createdAt();
        return entity;
    }

    public com.jobpilot.domain.agent.AgentDecision toDomain() {
        return com.jobpilot.domain.agent.AgentDecision.reconstitute(
            com.jobpilot.domain.agent.DecisionId.from(id),
            missionId, taskId,
            com.jobpilot.domain.agent.DecisionType.valueOf(decisionType.name()),
            reasoning, null, null,
            confidence, executed, createdAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMissionId() { return missionId; }
    public void setMissionId(UUID missionId) { this.missionId = missionId; }
    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public com.jobpilot.domain.agent.DecisionType getDecisionType() { return decisionType; }
    public void setDecisionType(com.jobpilot.domain.agent.DecisionType decisionType) { this.decisionType = decisionType; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public boolean isExecuted() { return executed; }
    public void setExecuted(boolean executed) { this.executed = executed; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
