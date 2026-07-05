package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.domain.ai.AiUsageLog;
import com.jobpilot.domain.ai.AiUsageLogId;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogEntity;
import com.jobpilot.infrastructure.persistence.ai.AiUsageLogJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
@ConditionalOnMissingBean(AiProviderPort.class)
public class FallbackAiProvider implements AiProviderPort {

    private final AiUsageLogJpaRepository usageLogJpaRepository;

    public FallbackAiProvider(AiUsageLogJpaRepository usageLogJpaRepository) {
        this.usageLogJpaRepository = usageLogJpaRepository;
    }

    @Override
    public String executePrompt(String systemPrompt, String userPrompt, String modelOverride,
                                double temperature, int maxTokens) {
        logUsage("prompt_execution", "fallback", "mock", 0, 0, 0, false);

        if (systemPrompt.contains("scorer") || systemPrompt.contains("ATS") || userPrompt.contains("Resume")) {
            return """
                {"atsScore": 78, "scoreBreakdown": {"keywords": 80, "format": 75, "experience": 82, "education": 70},
                 "missingKeywords": ["docker", "kubernetes", "terraform"],
                 "strengths": ["Strong technical background", "Clear career progression"],
                 "improvements": ["Add more quantifiable achievements", "Include relevant certifications"]}""";
        }
        if (systemPrompt.contains("gap") || systemPrompt.contains("skill")) {
            return """
                {"existingSkills": ["java", "spring", "sql", "git"],
                 "missingSkills": ["docker", "kubernetes", "aws", "ci/cd"],
                 "skillGaps": [{"skill": "docker", "category": "devops", "importance": "high",
                    "learningResources": ["Docker Deep Dive", "Docker Certified Associate"]}],
                 "summary": "Strong backend foundation, needs cloud and containerization skills."}""";
        }
        if (systemPrompt.contains("match") || systemPrompt.contains("matching")) {
            return """
                {"matchScore": 72.5, "matchBreakdown": {"skills": 70, "experience": 75, "education": 80, "seniority": 65},
                 "matchedSkills": ["java", "spring", "sql"],
                 "missingSkills": ["aws", "microservices", "event-driven"],
                 "recommendation": "Good fit for mid-level roles. Consider upskilling in cloud technologies."}""";
        }
        return "{\"content\": \"I processed your request successfully.\"}";
    }

    @Override
    public void logUsage(String useCase, String provider, String modelName,
                         int promptTokens, int completionTokens, int latencyMs, boolean cacheHit) {
        var log = new AiUsageLog(AiUsageLogId.generate(), null, useCase, provider, modelName,
            promptTokens, completionTokens, 0, latencyMs, cacheHit);
        usageLogJpaRepository.save(AiUsageLogEntity.fromDomain(log));
    }
}
