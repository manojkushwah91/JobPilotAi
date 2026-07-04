package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.domain.ai.AiUsageLog;
import com.jobpilot.domain.ai.AiUsageLogId;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogEntity;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Primary
@ConditionalOnProperty(name = "ai.ollama.base-url")
public class OllamaAiProvider implements AiProviderPort {

    private final RestTemplate rest;
    private final String baseUrl;
    private final String defaultModel;
    private final AiUsageLogJpaRepository usageLogJpaRepository;

    public OllamaAiProvider(
            @Value("${ai.ollama.base-url}") String baseUrl,
            @Value("${ai.ollama.model:llama3.2}") String defaultModel,
            AiUsageLogJpaRepository usageLogJpaRepository) {
        this.baseUrl = baseUrl;
        this.rest = new RestTemplate();
        this.defaultModel = defaultModel;
        this.usageLogJpaRepository = usageLogJpaRepository;
    }

    @Override
    public String executePrompt(String systemPrompt, String userPrompt, String modelOverride,
                                double temperature, int maxTokens) {
        var model = modelOverride != null ? modelOverride : defaultModel;

        var messages = java.util.List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        );

        var body = Map.of(
            "model", model,
            "messages", messages,
            "stream", false,
            "options", Map.of("temperature", temperature, "num_predict", maxTokens)
        );

        var start = System.currentTimeMillis();
        var response = rest.postForObject(baseUrl + "/api/chat", body, OllamaChatResponse.class);
        var latencyMs = (int) (System.currentTimeMillis() - start);

        logUsage("prompt_execution", "ollama", model, 0, 0, latencyMs, false);

        if (response != null && response.message() != null) {
            return response.message().content();
        }
        return "";
    }

    @Override
    public void logUsage(String useCase, String provider, String modelName,
                         int promptTokens, int completionTokens, int latencyMs, boolean cacheHit) {
        var log = new AiUsageLog(AiUsageLogId.generate(), null, useCase, provider, modelName,
            promptTokens, completionTokens, 0, latencyMs, cacheHit);
        usageLogJpaRepository.save(AiUsageLogEntity.fromDomain(log));
    }

    private record OllamaChatMessage(String role, String content) {}
    private record OllamaChatResponse(String model, OllamaChatMessage message, Boolean done) {}
}
