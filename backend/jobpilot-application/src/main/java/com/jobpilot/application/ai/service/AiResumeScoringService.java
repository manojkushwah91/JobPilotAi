package com.jobpilot.application.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.AiResumeScoringPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiResumeScoringService implements AiResumeScoringPort {

    private static final Logger log = LoggerFactory.getLogger(AiResumeScoringService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    @SuppressWarnings("unchecked")
    private AiResumeScoreResponse parseScore(String raw) {
        try {
            Map<String, Object> map = MAPPER.readValue(raw, new TypeReference<>() {});
            int atsScore = map.containsKey("atsScore") ? ((Number) map.get("atsScore")).intValue() : 0;
            Map<String, Object> breakdown = map.containsKey("scoreBreakdown") ? (Map<String, Object>) map.get("scoreBreakdown") : null;
            List<String> keywords = map.containsKey("missingKeywords") ? (List<String>) map.get("missingKeywords") : null;
            List<String> strengths = map.containsKey("strengths") ? (List<String>) map.get("strengths") : null;
            List<String> improvements = map.containsKey("improvements") ? (List<String>) map.get("improvements") : null;
            return new AiResumeScoreResponse(atsScore, breakdown, keywords, strengths, improvements);
        } catch (Exception e) {
            log.warn("Failed to parse AI resume score response: {}", e.getMessage());
            return new AiResumeScoreResponse(0, null, null, null, null);
        }
    }
}
