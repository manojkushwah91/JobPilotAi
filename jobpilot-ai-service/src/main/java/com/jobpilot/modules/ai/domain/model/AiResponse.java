package com.jobpilot.modules.ai.domain.model;

public record AiResponse(
    String content,
    FinishReason finishReason,
    TokenUsage usage,
    String modelUsed,
    long latencyMs
) {
    public AiResponse {
        if (content == null) throw new IllegalArgumentException("content must not be null");
        if (modelUsed == null || modelUsed.isBlank()) throw new IllegalArgumentException("modelUsed must not be blank");
    }
}
