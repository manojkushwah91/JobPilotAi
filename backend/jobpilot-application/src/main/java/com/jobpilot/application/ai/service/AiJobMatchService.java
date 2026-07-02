package com.jobpilot.application.ai.service;

import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiJobMatchPort;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import org.springframework.stereotype.Service;

@Service
public class AiJobMatchService implements AiJobMatchPort {

    private final AiProviderPort aiProvider;
    private final PromptTemplateRepository promptTemplateRepository;

    public AiJobMatchService(AiProviderPort aiProvider, PromptTemplateRepository promptTemplateRepository) {
        this.aiProvider = aiProvider;
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @Override
    public AiJobMatchResponse matchJob(AiJobMatchRequest request) {
        var template = promptTemplateRepository.findActiveByUseCase("job_matching")
            .orElseThrow(() -> new IllegalStateException("No active job_matching prompt template"));
        var userPrompt = template.userPromptTemplate()
            .replace("{{resumeId}}", request.resumeId())
            .replace("{{jobId}}", request.jobId());
        aiProvider.executePrompt(template.systemPrompt(), userPrompt,
            template.model(), template.temperature(), template.maxTokens());
        return new AiJobMatchResponse(0.0, null, null, null, null);
    }
}
