package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.domain.ai.AiUsageLog;
import com.jobpilot.domain.ai.AiUsageLogId;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogEntity;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogJpaRepository;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class DefaultAiProvider implements AiProviderPort {

    private final AiUsageLogJpaRepository usageLogJpaRepository;
    private final OpenAiService openAiService;
    private final String model;

    public DefaultAiProvider(AiUsageLogJpaRepository usageLogJpaRepository,
                              @Value("${ai.openai.api-key:}") String apiKey,
                              @Value("${ai.openai.model:gpt-4}") String model) {
        this.usageLogJpaRepository = usageLogJpaRepository;
        this.openAiService = apiKey != null && !apiKey.isBlank()
            ? new OpenAiService(apiKey, Duration.ofSeconds(30))
            : null;
        this.model = model;
    }

    @Override
    public String executePrompt(String systemPrompt, String userPrompt, String modelOverride, double temperature, int maxTokens) {
        if (openAiService == null) {
            logUsage("prompt_execution", "mock", modelOverride != null ? modelOverride : model, 0, 0, 0, false);
            return "{}";
        }
        var actualModel = modelOverride != null ? modelOverride : model;
        var messages = List.of(
            new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
            new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
        );
        var request = ChatCompletionRequest.builder()
            .model(actualModel)
            .messages(messages)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();
        var start = System.currentTimeMillis();
        var result = openAiService.createChatCompletion(request);
        var latency = (int) (System.currentTimeMillis() - start);
        var usage = result.getUsage();
        logUsage("prompt_execution", "openai", actualModel,
            usage != null ? (int) usage.getPromptTokens() : 0,
            usage != null ? (int) usage.getCompletionTokens() : 0,
            latency, false);
        return result.getChoices().get(0).getMessage().getContent();
    }

    @Override
    public void logUsage(String useCase, String provider, String modelName, int promptTokens, int completionTokens, int latencyMs, boolean cacheHit) {
        var log = new AiUsageLog(AiUsageLogId.generate(), null, useCase, provider, modelName,
            promptTokens, completionTokens, 0, latencyMs, cacheHit);
        usageLogJpaRepository.save(AiUsageLogEntity.fromDomain(log));
    }
}
