package com.jobpilot.application.ai.service;

import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.AiResumeScoringPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import org.springframework.stereotype.Service;

@Service
public class AiResumeScoringService implements AiResumeScoringPort {

    private final AiProviderPort aiProvider;
    private final PromptTemplateRepository promptTemplateRepository;

    public AiResumeScoringService(AiProviderPort aiProvider, PromptTemplateRepository promptTemplateRepository) {
        this.aiProvider = aiProvider;
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @Override
    public AiResumeScoreResponse scoreResume(AiResumeScoreRequest request) {
        var template = promptTemplateRepository.findActiveByUseCase("resume_scoring")
            .orElseThrow(() -> new IllegalStateException("No active resume_scoring prompt template"));
        var userPrompt = template.userPromptTemplate()
            .replace("{{resumeId}}", request.resumeId())
            .replace("{{jobDescription}}", request.jobDescription() != null ? request.jobDescription() : "");
        var result = aiProvider.executePrompt(template.systemPrompt(), userPrompt,
            template.model(), template.temperature(), template.maxTokens());
        return parseScore(result);
    }

    private AiResumeScoreResponse parseScore(String raw) {
        // Placeholder: in production, parse structured JSON from LLM response
        return new AiResumeScoreResponse(0, null, null, null, null);
    }
}
