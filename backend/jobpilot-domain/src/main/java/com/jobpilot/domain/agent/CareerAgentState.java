package com.jobpilot.domain.agent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class CareerAgentState {
    private final UUID userId;
    private AgentIdentity identity;
    private AgentPlan currentPlan;
    private Instant lastReflectionAt;
    private Instant lastWeeklyReviewAt;
    private int consecutiveFailures;
    private int totalApplicationsSubmitted;
    private int totalInterviewsScheduled;

    public CareerAgentState(UUID userId, AgentIdentity identity) {
        this.userId = Objects.requireNonNull(userId);
        this.identity = Objects.requireNonNull(identity);
        this.currentPlan = AgentPlan.empty();
        this.lastReflectionAt = Instant.now();
        this.lastWeeklyReviewAt = Instant.now();
    }

    public UUID userId() { return userId; }
    public AgentIdentity identity() { return identity; }
    public void updateIdentity(AgentIdentity identity) { this.identity = identity; }
    public AgentPlan currentPlan() { return currentPlan; }
    public void updatePlan(AgentPlan plan) { this.currentPlan = plan; }
    public Instant lastReflectionAt() { return lastReflectionAt; }
    public void markReflected() { this.lastReflectionAt = Instant.now(); }
    public Instant lastWeeklyReviewAt() { return lastWeeklyReviewAt; }
    public void markWeeklyReviewDone() { this.lastWeeklyReviewAt = Instant.now(); }
    public int consecutiveFailures() { return consecutiveFailures; }
    public void recordFailure() { this.consecutiveFailures++; }
    public void recordSuccess() { this.consecutiveFailures = 0; }
    public int totalApplicationsSubmitted() { return totalApplicationsSubmitted; }
    public void incrementApplications() { this.totalApplicationsSubmitted++; }
    public int totalInterviewsScheduled() { return totalInterviewsScheduled; }
    public void incrementInterviews() { this.totalInterviewsScheduled++; }
}
