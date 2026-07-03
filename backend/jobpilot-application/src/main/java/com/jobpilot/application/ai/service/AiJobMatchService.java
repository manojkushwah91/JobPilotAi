package com.jobpilot.application.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiJobMatchPort;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiJobMatchService implements AiJobMatchPort {

    private static final Logger log = LoggerFactory.getLogger(AiJobMatchService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        var result = aiProvider.executePrompt(template.systemPrompt(), userPrompt,
            template.model(), template.temperature(), template.maxTokens());
        return parseMatch(result);
    }

    @SuppressWarnings("unchecked")
    private AiJobMatchResponse parseMatch(String raw) {
        try {
            Map<String, Object> map = MAPPER.readValue(raw, new TypeReference<>() {});
            double score = map.containsKey("matchScore") ? ((Number) map.get("matchScore")).doubleValue() : 0.0;
            Map<String, Object> breakdown = map.containsKey("matchBreakdown") ? (Map<String, Object>) map.get("matchBreakdown") : null;
            List<String> matched = map.containsKey("matchedSkills") ? (List<String>) map.get("matchedSkills") : null;
            List<String> missing = map.containsKey("missingSkills") ? (List<String>) map.get("missingSkills") : null;
            String recommendation = map.containsKey("recommendation") ? (String) map.get("recommendation") : null;
            return new AiJobMatchResponse(score, breakdown, matched, missing, recommendation);
        } catch (Exception e) {
            log.warn("Failed to parse AI job match response: {}", e.getMessage());
            return new AiJobMatchResponse(0.0, null, null, null, null);
        }
    }
}
