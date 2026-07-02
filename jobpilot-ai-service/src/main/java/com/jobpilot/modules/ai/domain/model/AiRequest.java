package com.jobpilot.modules.ai.domain.model;

import java.util.List;

public record AiRequest(
    String model,
    List<AiMessage> messages,
    double temperature,
    int maxTokens,
    List<String> stopSequences,
    ResponseFormat responseFormat,
    List<AiTool> tools
) {
    public AiRequest {
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model must not be blank");
        if (messages == null || messages.isEmpty()) throw new IllegalArgumentException("messages must not be empty");
        if (temperature < 0 || temperature > 2) throw new IllegalArgumentException("temperature must be 0-2");
        if (maxTokens < 1 || maxTokens > 128_000) throw new IllegalArgumentException("maxTokens out of range");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String model;
        private List<AiMessage> messages;
        private double temperature = 0.7;
        private int maxTokens = 2048;
        private List<String> stopSequences = List.of();
        private ResponseFormat responseFormat = ResponseFormat.TEXT;
        private List<AiTool> tools = List.of();

        public Builder model(String model) { this.model = model; return this; }
        public Builder messages(List<AiMessage> messages) { this.messages = messages; return this; }
        public Builder temperature(double temperature) { this.temperature = temperature; return this; }
        public Builder maxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }
        public Builder stopSequences(List<String> stopSequences) { this.stopSequences = stopSequences; return this; }
        public Builder responseFormat(ResponseFormat responseFormat) { this.responseFormat = responseFormat; return this; }
        public Builder tools(List<AiTool> tools) { this.tools = tools; return this; }

        public AiRequest build() {
            return new AiRequest(model, messages, temperature, maxTokens, stopSequences, responseFormat, tools);
        }
    }
}
