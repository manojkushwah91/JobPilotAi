package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CoverLetterTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(CoverLetterTool.class);

    private final AiProviderPort aiProvider;
    private final CandidateProfileRepository profileRepository;

    public CoverLetterTool(AiProviderPort aiProvider, CandidateProfileRepository profileRepository) {
        this.aiProvider = aiProvider;
        this.profileRepository = profileRepository;
    }

    @Override
    public String name() {
        return "GENERATE_COVER_LETTER";
    }

    @Override
    public String description() {
        return "Generates a personalized cover letter for a job application";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing cover letter tool");

        var jobTitle = (String) input.getOrDefault("title", "");
        var company = (String) input.getOrDefault("company", "");
        var jobDescription = (String) input.getOrDefault("description", "");
        var userId = input.get("userId") instanceof java.util.UUID uid ? uid : null;

        if (jobDescription.isBlank()) {
            return Map.of("status", "error", "error", "No job description provided");
        }

        var profile = userId != null ? profileRepository.findByUserId(userId).orElse(null) : null;
        if (profile == null) {
            return Map.of("status", "error", "error", "No candidate profile found");
        }

        var systemPrompt = """
            You are a professional cover letter writer. Write a compelling, personalized
            cover letter for a job application. Return ONLY the cover letter text.
            Rules:
            - Professional but personable tone
            - Address the specific role and company
            - Highlight 2-3 most relevant skills/experiences
            - Show genuine interest in the company
            - Keep it to 3-4 paragraphs (300-400 words max)
            - Do not use generic templates
            - Do not fabricate experience
            """;

        var userPrompt = String.format("""
            Job Title: %s
            Company: %s

            Job Description:
            %s

            Candidate Profile:
            Name: %s
            Headline: %s
            Summary: %s
            Skills: %s
            Experience: %s
            Education: %s

            Write a personalized cover letter for this candidate applying to this position.
            """,
            jobTitle, company, jobDescription,
            profile.fullName(), profile.headline(), profile.summary(),
            profile.skills(), profile.experience(), profile.education()
        );

        try {
            var coverLetter = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.4, 1500);
            log.info("Cover letter generated for {} at {}", jobTitle, company);
            return Map.of(
                "status", "success",
                "coverLetter", coverLetter,
                "jobTitle", jobTitle,
                "company", company
            );
        } catch (Exception e) {
            log.error("Cover letter generation failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @Override
    public int timeoutSeconds() {
        return 90;
    }
}
