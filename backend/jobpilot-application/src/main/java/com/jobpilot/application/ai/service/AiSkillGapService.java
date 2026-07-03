package com.jobpilot.application.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.AiSkillGapPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiSkillGapService implements AiSkillGapPort {

    private static final Logger log = LoggerFactory.getLogger(AiSkillGapService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        var result = aiProvider.executePrompt(template.systemPrompt(), userPrompt,
            template.model(), template.temperature(), template.maxTokens());
        return parseGap(result);
    }

    @SuppressWarnings("unchecked")
    private AiSkillGapResponse parseGap(String raw) {
        try {
            Map<String, Object> map = MAPPER.readValue(raw, new TypeReference<>() {});
            List<String> existing = map.containsKey("existingSkills") ? (List<String>) map.get("existingSkills") : null;
            List<String> missing = map.containsKey("missingSkills") ? (List<String>) map.get("missingSkills") : null;
            List<SkillGapItem> gaps = map.containsKey("skillGaps") ? parseSkillGaps((List<Map<String, Object>>) map.get("skillGaps")) : null;
            String summary = map.containsKey("summary") ? (String) map.get("summary") : null;
            return new AiSkillGapResponse(existing, missing, gaps, summary);
        } catch (Exception e) {
            log.warn("Failed to parse AI skill gap response: {}", e.getMessage());
            return new AiSkillGapResponse(null, null, null, null);
        }
    }

    @SuppressWarnings("unchecked")
    private List<SkillGapItem> parseSkillGaps(List<Map<String, Object>> items) {
        if (items == null) return null;
        return items.stream().map(m -> new SkillGapItem(
            (String) m.get("skill"),
            (String) m.get("category"),
            (String) m.get("importance"),
            m.containsKey("learningResources") ? (List<String>) m.get("learningResources") : null
        )).toList();
    }
}
