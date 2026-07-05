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
        return "Generates a tailored cover letter for a specific job posting";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing cover letter generator tool");

        var resumeContent = (String) input.getOrDefault("resumeContent", "");
        var jobDescription = (String) input.getOrDefault("jobDescription", "");
        var companyName = (String) input.getOrDefault("companyName", "");
        var jobTitle = (String) input.getOrDefault("jobTitle", "");
        var userName = (String) input.getOrDefault("userName", "Applicant");

        var systemPrompt = "You are an expert cover letter writer. Given a resume and job description, " +
            "write a compelling, professional cover letter that highlights relevant experience and skills. " +
            "The cover letter should be concise (3-4 paragraphs), personalized for the company, " +
            "and demonstrate genuine interest. Return the cover letter as plain text.";

        var userPrompt = String.format(
            "Applicant Name: %s\nResume:\n%s\n\nJob Description:\n%s\n\nCompany: %s\nJob Title: %s",
            userName, resumeContent, jobDescription, companyName, jobTitle
        );

        var result = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.7, 1000);

        return Map.of(
            "status", "success",
            "coverLetter", result,
            "companyName", companyName,
            "jobTitle", jobTitle
        );
    }

    @Override
    public int timeoutSeconds() {
        return 60;
    }
}
