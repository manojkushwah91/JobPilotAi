package com.jobpilot.modules.ai.domain.model;

public record AiMessage(AiMessageRole role, String content, String name) {
    public AiMessage {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }

    public static AiMessage system(String content) {
        return new AiMessage(AiMessageRole.SYSTEM, content, null);
    }

    public static AiMessage user(String content) {
        return new AiMessage(AiMessageRole.USER, content, null);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage(AiMessageRole.ASSISTANT, content, null);
    }
}
