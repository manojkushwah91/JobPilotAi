package com.jobpilot.application.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.AiSkillGapPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.domain.resume.ResumeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiSkillGapService implements AiSkillGapPort {

    private static final Logger log = LoggerFactory.getLogger(AiSkillGapService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiProviderPort aiProvider;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ResumeRepository resumeRepository;

    public AiSkillGapService(AiProviderPort aiProvider,
                             PromptTemplateRepository promptTemplateRepository,
                             ResumeRepository resumeRepository) {
        this.aiProvider = aiProvider;
        this.promptTemplateRepository = promptTemplateRepository;
        this.resumeRepository = resumeRepository;
    }

    @Override
    public AiSkillGapResponse analyzeSkillGap(AiSkillGapRequest request) {
        var resumeId = ResumeId.from(UUID.fromString(request.resumeId()));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + request.resumeId()));

        var sectionsText = new StringBuilder();
        for (var section : resume.sections()) {
            sectionsText.append(section.type()).append(": ")
                .append(section.content()).append("\n");
        }

        var template = promptTemplateRepository.findActiveByUseCase("skill_gap")
            .orElse(null);

        String result;
        if (template != null) {
            var userPrompt = template.userPromptTemplate()
                .replace("{{resumeContent}}", sectionsText.toString())
                .replace("{{targetRole}}", request.targetRole());
            result = aiProvider.executePrompt(template.systemPrompt(), userPrompt,
                template.model(), template.temperature(), template.maxTokens());
        } else {
            var system = "You are a skill gap analyst. Compare the candidate's resume against the target role and return a JSON object with existingSkills (array), missingSkills (array), skillGaps (array of {skill, category, importance, learningResources}), and summary (string).";
            var user = "Resume:\n" + sectionsText + "\n\nTarget Role:\n" + request.targetRole();
            result = aiProvider.executePrompt(system, user, null, 0.3, 2000);
        }

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
