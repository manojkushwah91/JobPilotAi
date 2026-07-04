package com.jobpilot.application.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.AiResumeScoringPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.domain.resume.ResumeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiResumeScoringService implements AiResumeScoringPort {

    private static final Logger log = LoggerFactory.getLogger(AiResumeScoringService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiProviderPort aiProvider;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ResumeRepository resumeRepository;

    public AiResumeScoringService(AiProviderPort aiProvider,
                                  PromptTemplateRepository promptTemplateRepository,
                                  ResumeRepository resumeRepository) {
        this.aiProvider = aiProvider;
        this.promptTemplateRepository = promptTemplateRepository;
        this.resumeRepository = resumeRepository;
    }

    @Override
    @Transactional
    public AiResumeScoreResponse scoreResume(AiResumeScoreRequest request) {
        var resumeId = ResumeId.from(UUID.fromString(request.resumeId()));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + request.resumeId()));

        var sectionsText = new StringBuilder();
        for (var section : resume.sections()) {
            sectionsText.append(section.type()).append(": ")
                .append(section.content()).append("\n");
        }

        var template = promptTemplateRepository.findActiveByUseCase("resume_scoring")
            .orElse(null);

        String result;
        if (template != null) {
            var userPrompt = template.userPromptTemplate()
                .replace("{{resumeContent}}", sectionsText.toString())
                .replace("{{jobDescription}}", request.jobDescription() != null ? request.jobDescription() : "");
            result = aiProvider.executePrompt(template.systemPrompt(), userPrompt,
                template.model(), template.temperature(), template.maxTokens());
        } else {
            var systemPrompt = "You are an expert ATS resume scorer. Analyze the resume against the job description and return a JSON object with atsScore (0-100), scoreBreakdown (object), missingKeywords (array), strengths (array), and improvements (array).";
            var userPrompt = "Resume:\n" + sectionsText + "\n\nJob Description:\n" + (request.jobDescription() != null ? request.jobDescription() : "No job description provided. Score based on general resume quality.");
            result = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.3, 2000);
        }

        var scoreResponse = parseScore(result);

        resume.updateAtsScore(scoreResponse.atsScore(), scoreResponse.scoreBreakdown());
        resumeRepository.save(resume);

        return scoreResponse;
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
