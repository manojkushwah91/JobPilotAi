package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.domain.ai.AiUsageLog;
import com.jobpilot.domain.ai.AiUsageLogId;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogEntity;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogJpaRepository;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "ai.openai.api-key")
public class OpenAiProvider implements AiProviderPort {

    private final OpenAiService service;
    private final String defaultModel;
    private final AiUsageLogJpaRepository usageLogJpaRepository;

    public OpenAiProvider(
            @Value("${ai.openai.api-key}") String apiKey,
            @Value("${ai.openai.model:gpt-4}") String defaultModel,
            AiUsageLogJpaRepository usageLogJpaRepository) {
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(30));
        this.defaultModel = defaultModel;
        this.usageLogJpaRepository = usageLogJpaRepository;
    }

    @Override
    public String executePrompt(String systemPrompt, String userPrompt, String modelOverride,
                                double temperature, int maxTokens) {
        var model = modelOverride != null ? modelOverride : defaultModel;
        var messages = java.util.List.of(
            new ChatMessage("system", systemPrompt),
            new ChatMessage("user", userPrompt)
        );
        var request = ChatCompletionRequest.builder()
            .model(model)
            .messages(messages)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        var start = System.currentTimeMillis();
        var result = service.createChatCompletion(request);
        var latencyMs = (int) (System.currentTimeMillis() - start);

        var choice = result.getChoices().get(0);
        var content = choice.getMessage().getContent();

        var usage = result.getUsage();
        logUsage("prompt_execution", "openai", model,
            (int) usage.getPromptTokens(),
            (int) usage.getCompletionTokens(),
            latencyMs, false);

        return content;
    }

    @Override
    public void logUsage(String useCase, String provider, String modelName,
                         int promptTokens, int completionTokens, int latencyMs, boolean cacheHit) {
        var log = new AiUsageLog(AiUsageLogId.generate(), null, useCase, provider, modelName,
            promptTokens, completionTokens, 0, latencyMs, cacheHit);
        usageLogJpaRepository.save(AiUsageLogEntity.fromDomain(log));
    }
}
