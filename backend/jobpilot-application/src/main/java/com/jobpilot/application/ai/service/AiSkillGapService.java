package com.jobpilot.application.ai.service;

import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.AiSkillGapPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import org.springframework.stereotype.Service;

@Service
public class AiSkillGapService implements AiSkillGapPort {

    private final AiProviderPort aiProvider;
    private final PromptTemplateRepository promptTemplateRepository;

    public AiSkillGapService(AiProviderPort aiProvider, PromptTemplateRepository promptTemplateRepository) {
        this.aiProvider = aiProvider;
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @Override
    public AiSkillGapResponse analyzeSkillGap(AiSkillGapRequest request) {
        var template = promptTemplateRepository.findActiveByUseCase("skill_gap")
            .orElseThrow(() -> new IllegalStateException("No active skill_gap prompt template"));
        var userPrompt = template.userPromptTemplate()
            .replace("{{resumeId}}", request.resumeId())
            .replace("{{targetRole}}", request.targetRole());
        aiProvider.executePrompt(template.systemPrompt(), userPrompt,
            template.model(), template.temperature(), template.maxTokens());
        return new AiSkillGapResponse(null, null, null, null);
    }
}
