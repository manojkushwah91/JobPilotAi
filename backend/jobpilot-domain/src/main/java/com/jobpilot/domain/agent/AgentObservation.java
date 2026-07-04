package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AgentObservation extends BaseAggregateRoot {

    private ObservationId observationId;
    private UUID missionId;
    private UUID taskId;
    private ObservationType type;
    private String source;
    private String description;
    private Map<String, Object> data;
    private double relevanceScore;
    private boolean actionable;
    private final Instant createdAt;

    private AgentObservation(ObservationId observationId, UUID missionId, ObservationType type,
                              String source, String description) {
        super(observationId.value());
        this.observationId = observationId;
        this.missionId = missionId;
        this.type = type;
        this.source = source;
        this.description = description;
        this.relevanceScore = 0.5;
        this.actionable = false;
        this.createdAt = Instant.now();
    }

    public static AgentObservation create(UUID missionId, ObservationType type,
                                           String source, String description) {
        return new AgentObservation(ObservationId.generate(), missionId, type, source, description);
    }

    public static AgentObservation reconstitute(ObservationId observationId, UUID missionId,
                                                  UUID taskId, ObservationType type, String source,
                                                  String description, Map<String, Object> data,
                                                  double relevanceScore, boolean actionable,
                                                  Instant createdAt) {
        var o = new AgentObservation(observationId, missionId, type, source, description);
        o.taskId = taskId;
        o.data = data;
        o.relevanceScore = relevanceScore;
        o.actionable = actionable;
        return o;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void setRelevanceScore(double score) {
        this.relevanceScore = Math.max(0.0, Math.min(1.0, score));
    }

    public void markActionable() {
        this.actionable = true;
    }

    public ObservationId observationId() { return observationId; }
    public UUID missionId() { return missionId; }
    public UUID taskId() { return taskId; }
    public ObservationType type() { return type; }
    public String source() { return source; }
    public String description() { return description; }
    public Map<String, Object> data() { return data; }
    public double relevanceScore() { return relevanceScore; }
    public boolean isActionable() { return actionable; }
    public Instant createdAt() { return createdAt; }
}
