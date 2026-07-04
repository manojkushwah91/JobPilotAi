package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ResumeTailoringTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ResumeTailoringTool.class);

    private final AiProviderPort aiProvider;

    public ResumeTailoringTool(AiProviderPort aiProvider) {
        this.aiProvider = aiProvider;
    }

    @Override
    public String name() {
        return "TAILOR_RESUME";
    }

    @Override
    public String description() {
        return "Tailors the resume for a specific job posting to maximize ATS score";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing resume tailoring tool");

        var resumeContent = (String) input.getOrDefault("resumeContent", "");
        var jobDescription = (String) input.getOrDefault("jobDescription", "");
        var companyName = (String) input.getOrDefault("companyName", "");

        var systemPrompt = "You are an expert resume tailor. Given a resume and job description, " +
            "optimize the resume to match the job requirements while maintaining authenticity. " +
            "Return a JSON object with: tailoredResume, changes, atsScoreImprovement, keywords.";

        var userPrompt = String.format(
            "Original Resume:\n%s\n\nJob Description:\n%s\n\nCompany: %s",
            resumeContent, jobDescription, companyName
        );

        var result = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.3, 3000);

        return Map.of(
            "status", "success",
            "tailoredContent", result,
            "companyName", companyName
        );
    }

    @Override
    public int timeoutSeconds() {
        return 90;
    }
}
