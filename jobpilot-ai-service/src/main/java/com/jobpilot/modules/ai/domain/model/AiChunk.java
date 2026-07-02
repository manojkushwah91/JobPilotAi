package com.jobpilot.modules.ai.domain.model;

public record AiChunk(String content, FinishReason finishReason) {
    public static AiChunk delta(String content) {
        return new AiChunk(content, null);
    }

    public static AiChunk done() {
        return new AiChunk("", FinishReason.STOP);
    }

    public boolean isLast() {
        return finishReason != null;
    }
}
