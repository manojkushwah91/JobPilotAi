package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CoverLetterGeneratorTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(CoverLetterGeneratorTool.class);

    private final AiProviderPort aiProvider;

    public CoverLetterGeneratorTool(AiProviderPort aiProvider) {
        this.aiProvider = aiProvider;
    }

    @Override
    public String name() {
        return "GENERATE_COVER_LETTER";
    }

    @Override
    public String description() {
        return "Generates a personalized cover letter for a specific job application";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing cover letter generator tool");

        var candidateProfile = (String) input.getOrDefault("candidateProfile", "");
        var jobDescription = (String) input.getOrDefault("jobDescription", "");
        var companyName = (String) input.getOrDefault("companyName", "");
        var tone = (String) input.getOrDefault("tone", "professional");

        var systemPrompt = "You are an expert cover letter writer. Generate a personalized, " +
            "compelling cover letter that matches the candidate's profile with the job requirements. " +
            "Never use generic templates. Return a JSON object with: coverLetter, wordCount, keyPoints.";

        var userPrompt = String.format(
            "Candidate Profile:\n%s\n\nJob Description:\n%s\n\nCompany: %s\nTone: %s",
            candidateProfile, jobDescription, companyName, tone
        );

        var result = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.4, 2000);

        return Map.of(
            "status", "success",
            "coverLetter", result,
            "companyName", companyName,
            "tone", tone
        );
    }

    @Override
    public int timeoutSeconds() {
        return 60;
    }
}
