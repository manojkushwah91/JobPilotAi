package com.jobpilot.application.agent.ports;

public interface AiProviderPort {

    String executePrompt(String systemPrompt, String userPrompt, String model,
                          double temperature, int maxTokens);

    String executePromptWithContext(String systemPrompt, String userPrompt,
                                     String context, String model,
                                     double temperature, int maxTokens);

    default boolean isAvailable() {
        return true;
    }

    default String providerName() {
        return "default";
    }
}
