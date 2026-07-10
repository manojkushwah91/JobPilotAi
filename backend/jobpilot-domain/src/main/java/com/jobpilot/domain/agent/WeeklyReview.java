package com.jobpilot.domain.agent;

import java.time.LocalDate;
import java.util.*;

public class WeeklyReview {
    private final UUID reviewId;
    private final UUID userId;
    private final LocalDate weekStart;
    private final LocalDate weekEnd;
    private final Map<String, Double> metrics;
    private final List<String> insights;
    private final List<String> recommendations;
    private final String strategyChange;
    private final boolean planNeedsUpdate;

    public WeeklyReview(UUID userId, LocalDate weekStart, LocalDate weekEnd,
                         Map<String, Double> metrics, List<String> insights,
                         List<String> recommendations, String strategyChange,
                         boolean planNeedsUpdate) {
        this.reviewId = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId);
        this.weekStart = Objects.requireNonNull(weekStart);
        this.weekEnd = Objects.requireNonNull(weekEnd);
        this.metrics = metrics != null ? new HashMap<>(metrics) : Map.of();
        this.insights = insights != null ? new ArrayList<>(insights) : List.of();
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : List.of();
        this.strategyChange = strategyChange;
        this.planNeedsUpdate = planNeedsUpdate;
    }

    public UUID reviewId() { return reviewId; }
    public UUID userId() { return userId; }
    public LocalDate weekStart() { return weekStart; }
    public LocalDate weekEnd() { return weekEnd; }
    public Map<String, Double> metrics() { return Collections.unmodifiableMap(metrics); }
    public List<String> insights() { return Collections.unmodifiableList(insights); }
    public List<String> recommendations() { return Collections.unmodifiableList(recommendations); }
    public Optional<String> strategyChange() { return Optional.ofNullable(strategyChange); }
    public boolean planNeedsUpdate() { return planNeedsUpdate; }

    public String toBriefing() {
        var sb = new StringBuilder();
        sb.append("=== Weekly Review (").append(weekStart).append(" to ").append(weekEnd).append(") ===\n\n");
        sb.append("Metrics:\n");
        for (var e : metrics.entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        sb.append("\nKey Insights:\n");
        for (var i : insights) sb.append("  - ").append(i).append("\n");
        sb.append("\nRecommendations:\n");
        for (var r : recommendations) sb.append("  - ").append(r).append("\n");
        if (strategyChange != null) {
            sb.append("\nStrategy Adjustment: ").append(strategyChange).append("\n");
        }
        return sb.toString();
    }
}
