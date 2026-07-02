package com.jobpilot.modules.ai.domain.model;

public record TokenUsage(int promptTokens, int completionTokens, int totalTokens) {
    public TokenUsage {
        if (promptTokens < 0) throw new IllegalArgumentException("promptTokens must be non-negative");
        if (completionTokens < 0) throw new IllegalArgumentException("completionTokens must be non-negative");
        if (totalTokens < 0) throw new IllegalArgumentException("totalTokens must be non-negative");
    }

    public static TokenUsage empty() {
        return new TokenUsage(0, 0, 0);
    }

    public long estimatedCostMicroUsd(String model) {
        return ModelCost.estimate(model, promptTokens, completionTokens);
    }
}
