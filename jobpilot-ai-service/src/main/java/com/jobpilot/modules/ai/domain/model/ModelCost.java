package com.jobpilot.modules.ai.domain.model;

import java.util.Map;

public final class ModelCost {
    private ModelCost() {}

    private static final Map<String, long[]> PRICING = Map.of(
        "gpt-4",            new long[]{30, 60},
        "gpt-4-turbo",      new long[]{10, 30},
        "gpt-3.5-turbo",    new long[]{0.5, 1.5},
        "claude-3-opus",    new long[]{15, 75},
        "claude-3-sonnet",  new long[]{3, 15},
        "claude-3-haiku",   new long[]{0.25, 1.25},
        "gemini-1.5-pro",   new long[]{3.5, 10.5},
        "gemini-1.5-flash", new long[]{0.075, 0.30},
        "llama3",           new long[]{0, 0},
        "mixtral",          new long[]{0, 0}
    );

    private static final long DEFAULT_INPUT = 10;
    private static final long DEFAULT_OUTPUT = 30;

    public static long estimate(String model, int promptTokens, int completionTokens) {
        long[] rates = PRICING.getOrDefault(model, new long[]{DEFAULT_INPUT, DEFAULT_OUTPUT});
        long inputCost = (long) promptTokens * rates[0] / 1_000_000;
        long outputCost = (long) completionTokens * rates[1] / 1_000_000;
        return inputCost + outputCost;
    }
}
