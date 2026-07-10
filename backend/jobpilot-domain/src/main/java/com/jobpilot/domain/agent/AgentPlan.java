package com.jobpilot.domain.agent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class AgentPlan {
    private final UUID planId;
    private final UUID missionId;
    private final String strategyNarrative;
    private final List<PlannedAction> actions;
    private final LocalDate horizon;
    private final Instant createdAt;
    private Instant lastRevisedAt;

    public AgentPlan(UUID missionId, String strategyNarrative,
                      List<PlannedAction> actions, LocalDate horizon) {
        this.planId = UUID.randomUUID();
        this.missionId = Objects.requireNonNull(missionId);
        this.strategyNarrative = Objects.requireNonNull(strategyNarrative);
        this.actions = new ArrayList<>(actions);
        this.horizon = Objects.requireNonNull(horizon);
        this.createdAt = Instant.now();
        this.lastRevisedAt = Instant.now();
    }

    public static AgentPlan empty() {
        return new AgentPlan(UUID.randomUUID(), "No plan", List.of(), LocalDate.now());
    }

    public UUID planId() { return planId; }
    public UUID missionId() { return missionId; }
    public String strategyNarrative() { return strategyNarrative; }
    public List<PlannedAction> actions() { return Collections.unmodifiableList(actions); }
    public LocalDate horizon() { return horizon; }
    public Instant createdAt() { return createdAt; }
    public Instant lastRevisedAt() { return lastRevisedAt; }
    public void markRevised() { this.lastRevisedAt = Instant.now(); }

    public List<PlannedAction> pendingActions() {
        return actions.stream()
            .filter(a -> a.status() == PlannedAction.Status.PENDING)
            .sorted(Comparator.comparingInt(PlannedAction::priority).reversed())
            .toList();
    }

    public void addAction(PlannedAction action) {
        actions.add(action);
    }

    public void completeAction(UUID actionId) {
        actions.stream()
            .filter(a -> a.actionId().equals(actionId))
            .findFirst()
            .ifPresent(a -> a.complete());
    }

    public String toPromptContext() {
        var sb = new StringBuilder();
        sb.append("Current Strategy: ").append(strategyNarrative).append("\n");
        sb.append("Plan horizon: ").append(horizon).append("\n\n");
        sb.append("Planned actions:\n");
        for (var a : actions) {
            sb.append("- [").append(a.status()).append("] ")
              .append("P").append(a.priority()).append(": ")
              .append(a.actionType()).append(" - ").append(a.description())
              .append(" (by ").append(a.scheduledFor()).append(")")
              .append("\n");
            if (a.reasoning() != null) {
                sb.append("  Reasoning: ").append(a.reasoning()).append("\n");
            }
        }
        return sb.toString();
    }

    public static class PlannedAction {
        private final UUID actionId;
        private final String actionType;
        private final String description;
        private final String reasoning;
        private final int priority;
        private final LocalDate scheduledFor;
        private Status status;
        private Map<String, Object> input;

        public PlannedAction(String actionType, String description, String reasoning,
                              int priority, LocalDate scheduledFor) {
            this.actionId = UUID.randomUUID();
            this.actionType = Objects.requireNonNull(actionType);
            this.description = Objects.requireNonNull(description);
            this.reasoning = reasoning;
            this.priority = priority;
            this.scheduledFor = Objects.requireNonNull(scheduledFor);
            this.status = Status.PENDING;
            this.input = new HashMap<>();
        }

        public UUID actionId() { return actionId; }
        public String actionType() { return actionType; }
        public String description() { return description; }
        public String reasoning() { return reasoning; }
        public int priority() { return priority; }
        public LocalDate scheduledFor() { return scheduledFor; }
        public Status status() { return status; }
        public void complete() { this.status = Status.COMPLETED; }
        public void fail() { this.status = Status.FAILED; }
        public void skip() { this.status = Status.SKIPPED; }
        public Map<String, Object> input() { return input; }
        public void withInput(Map<String, Object> input) { this.input = input; }

        public enum Status {
            PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED
        }
    }
}
