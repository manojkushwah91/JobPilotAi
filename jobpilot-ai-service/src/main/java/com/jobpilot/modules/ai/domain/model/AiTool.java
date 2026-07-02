package com.jobpilot.modules.ai.domain.model;

public record AiTool(String name, String description, String jsonSchema) {
    public AiTool {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
    }
}
