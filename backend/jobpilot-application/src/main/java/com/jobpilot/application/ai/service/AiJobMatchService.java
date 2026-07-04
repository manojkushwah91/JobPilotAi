package com.jobpilot.application.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.AiJobMatchPort;
import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.resume.ResumeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiJobMatchService implements AiJobMatchPort {

    private static final Logger log = LoggerFactory.getLogger(AiJobMatchService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiProviderPort aiProvider;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;

    public AiJobMatchService(AiProviderPort aiProvider,
                             PromptTemplateRepository promptTemplateRepository,
                             ResumeRepository resumeRepository,
                             JobRepository jobRepository) {
        this.aiProvider = aiProvider;
        this.promptTemplateRepository = promptTemplateRepository;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public AiJobMatchResponse matchJob(AiJobMatchRequest request) {
        var resumeId = ResumeId.from(UUID.fromString(request.resumeId()));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + request.resumeId()));

        var jobId = JobId.from(UUID.fromString(request.jobId()));
        var job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + request.jobId()));

        var sectionsText = new StringBuilder();
        for (var section : resume.sections()) {
            sectionsText.append(section.type()).append(": ")
                .append(section.content()).append("\n");
        }

        var jobText = job.title() + "\n" + job.description();

        var template = promptTemplateRepository.findActiveByUseCase("job_matching")
            .orElse(null);

        String result;
        if (template != null) {
            var userPrompt = template.userPromptTemplate()
                .replace("{{resumeContent}}", sectionsText.toString())
                .replace("{{jobContent}}", jobText);
            result = aiProvider.executePrompt(template.systemPrompt(), userPrompt,
                template.model(), template.temperature(), template.maxTokens());
        } else {
            var system = "You are a job matching expert. Compare the candidate's resume against the job posting and return a JSON object with matchScore (0-100), matchBreakdown (object), matchedSkills (array), missingSkills (array), and recommendation (string).";
            var user = "Resume:\n" + sectionsText + "\n\nJob:\n" + jobText;
            result = aiProvider.executePrompt(system, user, null, 0.3, 2000);
        }

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
