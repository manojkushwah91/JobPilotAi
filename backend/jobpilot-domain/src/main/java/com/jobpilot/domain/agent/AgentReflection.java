package com.jobpilot.domain.agent;

import java.time.Instant;
import java.util.*;

public class AgentReflection {
    private final UUID reflectionId;
    private final UUID userId;
    private final String taskType;
    private final String taskDescription;
    private final boolean success;
    private final String narrative;
    private final String why;
    private final List<String> lessons;
    private final String newKnowledge;
    private final Instant timestamp;

    public AgentReflection(UUID userId, String taskType, String taskDescription,
                            boolean success, String narrative, String why,
                            List<String> lessons, String newKnowledge) {
        this.reflectionId = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId);
        this.taskType = Objects.requireNonNull(taskType);
        this.taskDescription = Objects.requireNonNull(taskDescription);
        this.success = success;
        this.narrative = Objects.requireNonNull(narrative);
        this.why = Objects.requireNonNull(why);
        this.lessons = lessons != null ? new ArrayList<>(lessons) : List.of();
        this.newKnowledge = newKnowledge;
        this.timestamp = Instant.now();
    }

    public UUID reflectionId() { return reflectionId; }
    public UUID userId() { return userId; }
    public String taskType() { return taskType; }
    public String taskDescription() { return taskDescription; }
    public boolean success() { return success; }
    public String narrative() { return narrative; }
    public String why() { return why; }
    public List<String> lessons() { return Collections.unmodifiableList(lessons); }
    public Optional<String> newKnowledge() { return Optional.ofNullable(newKnowledge); }
    public Instant timestamp() { return timestamp; }

    public String toPromptContext() {
        var sb = new StringBuilder();
        sb.append("Task: ").append(taskDescription).append("\n");
        sb.append("Type: ").append(taskType).append("\n");
        sb.append("Result: ").append(success ? "SUCCESS" : "FAILURE").append("\n");
        sb.append("What happened: ").append(narrative).append("\n");
        sb.append("Why: ").append(why).append("\n");
        if (!lessons.isEmpty()) {
            sb.append("Lessons learned:\n");
            for (var l : lessons) sb.append("- ").append(l).append("\n");
        }
        return sb.toString();
    }
}
