package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.agent.ports.AiProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class AgentAiProviderAdapter implements AiProviderPort {

    private static final Logger log = LoggerFactory.getLogger(AgentAiProviderAdapter.class);

    private final OllamaAiProvider ollamaProvider;
    private final OpenAiProvider openAiProvider;

    public AgentAiProviderAdapter(OllamaAiProvider ollamaProvider, OpenAiProvider openAiProvider) {
        this.ollamaProvider = ollamaProvider;
        this.openAiProvider = openAiProvider;
    }

    @Override
    public String executePrompt(String systemPrompt, String userPrompt, String model,
                                double temperature, int maxTokens) {
        try {
            return ollamaProvider.executePrompt(systemPrompt, userPrompt, model, temperature, maxTokens);
        } catch (Exception e) {
            log.warn("Ollama failed, falling back to OpenAI: {}", e.getMessage());
        }
        try {
            return openAiProvider.executePrompt(systemPrompt, userPrompt, model, temperature, maxTokens);
        } catch (Exception e) {
            log.error("All AI providers failed: {}", e.getMessage());
            return "I'm sorry, the AI service is currently unavailable. Please try again later.";
        }
    }

    @Override
    public String executePromptWithContext(String systemPrompt, String userPrompt,
                                           String context, String model,
                                           double temperature, int maxTokens) {
        var fullPrompt = userPrompt;
        if (context != null && !context.isEmpty()) {
            fullPrompt = "Context:\n" + context + "\n\n" + userPrompt;
        }
        return executePrompt(systemPrompt, fullPrompt, model, temperature, maxTokens);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "ollama-openai-fallback";
    }
}
