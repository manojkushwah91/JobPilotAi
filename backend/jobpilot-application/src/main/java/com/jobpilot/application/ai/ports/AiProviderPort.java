package com.jobpilot.application.ai.ports;

public interface AiProviderPort {
    String executePrompt(String systemPrompt, String userPrompt, String model, double temperature, int maxTokens);
    void logUsage(String useCase, String provider, String model, int promptTokens, int completionTokens, int latencyMs, boolean cacheHit);
}
