package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AgentDecision extends BaseAggregateRoot {

    private DecisionId decisionId;
    private UUID missionId;
    private UUID taskId;
    private DecisionType type;
    private String reasoning;
    private Map<String, Object> context;
    private Map<String, Object> decision;
    private double confidence;
    private boolean executed;
    private final Instant createdAt;

    private AgentDecision(DecisionId decisionId, UUID missionId, DecisionType type, String reasoning) {
        super(decisionId.value());
        this.decisionId = decisionId;
        this.missionId = missionId;
        this.type = type;
        this.reasoning = reasoning;
        this.confidence = 0.5;
        this.executed = false;
        this.createdAt = Instant.now();
    }

    public static AgentDecision create(UUID missionId, DecisionType type, String reasoning) {
        return new AgentDecision(DecisionId.generate(), missionId, type, reasoning);
    }

    public static AgentDecision reconstitute(DecisionId decisionId, UUID missionId, UUID taskId,
                                              DecisionType type, String reasoning,
                                              Map<String, Object> context, Map<String, Object> decision,
                                              double confidence, boolean executed, Instant createdAt) {
        var d = new AgentDecision(decisionId, missionId, type, reasoning);
        d.taskId = taskId;
        d.context = context;
        d.decision = decision;
        d.confidence = confidence;
        d.executed = executed;
        return d;
    }

    public void execute() {
        this.executed = true;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public void setDecision(Map<String, Object> decision) {
        this.decision = decision;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }

    public DecisionId decisionId() { return decisionId; }
    public UUID missionId() { return missionId; }
    public UUID taskId() { return taskId; }
    public DecisionType type() { return type; }
    public String reasoning() { return reasoning; }
    public Map<String, Object> context() { return context; }
    public Map<String, Object> decision() { return decision; }
    public double confidence() { return confidence; }
    public boolean isExecuted() { return executed; }
    public Instant createdAt() { return createdAt; }
}
