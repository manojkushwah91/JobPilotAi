package com.jobpilot.domain.agent;

import java.time.Instant;
import java.util.*;

public class AgentEpisode {
    private final UUID episodeId;
    private final UUID userId;
    private final String episodeType;
    private final String title;
    private final String narrative;
    private final boolean success;
    private final List<String> lessons;
    private final Map<String, Object> context;
    private final Instant occurredAt;
    private double significance;

    public AgentEpisode(UUID userId, String episodeType, String title,
                         String narrative, boolean success, List<String> lessons,
                         Map<String, Object> context, double significance) {
        this.episodeId = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId);
        this.episodeType = Objects.requireNonNull(episodeType);
        this.title = Objects.requireNonNull(title);
        this.narrative = Objects.requireNonNull(narrative);
        this.success = success;
        this.lessons = lessons != null ? new ArrayList<>(lessons) : List.of();
        this.context = context != null ? new HashMap<>(context) : Map.of();
        this.occurredAt = Instant.now();
        this.significance = significance;
    }

    public UUID episodeId() { return episodeId; }
    public UUID userId() { return userId; }
    public String episodeType() { return episodeType; }
    public String title() { return title; }
    public String narrative() { return narrative; }
    public boolean success() { return success; }
    public List<String> lessons() { return Collections.unmodifiableList(lessons); }
    public Map<String, Object> context() { return Collections.unmodifiableMap(context); }
    public Instant occurredAt() { return occurredAt; }
    public double significance() { return significance; }
    public void boostSignificance(double amount) { this.significance += amount; }

    public String toSummary() {
        var sb = new StringBuilder();
        sb.append("[").append(episodeType).append("] ").append(title).append("\n");
        sb.append("  Narrative: ").append(narrative).append("\n");
        sb.append("  Outcome: ").append(success ? "SUCCESS" : "FAILURE").append("\n");
        if (!lessons.isEmpty()) {
            sb.append("  Lessons:\n");
            for (var l : lessons) sb.append("    - ").append(l).append("\n");
        }
        return sb.toString();
    }
}
