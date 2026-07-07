package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ResumeTailoringTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ResumeTailoringTool.class);

    private final AiProviderPort aiProvider;
    private final CandidateProfileRepository profileRepository;

    public ResumeTailoringTool(AiProviderPort aiProvider, CandidateProfileRepository profileRepository) {
        this.aiProvider = aiProvider;
        this.profileRepository = profileRepository;
    }

    @Override
    public String name() {
        return "TAILOR_RESUME";
    }

    @Override
    public String description() {
        return "Tailors the user's resume to match a specific job description";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing resume tailoring tool");

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

        var resumeText = profile.resumeText();
        if (resumeText == null || resumeText.isBlank()) {
            resumeText = buildProfileSummary(profile);
        }

        var systemPrompt = """
            You are an expert resume writer and career coach. Your task is to tailor a resume
            to match a specific job description. Return ONLY the tailored resume text, no commentary.
            Rules:
            - Keep all real experience and skills (never fabricate)
            - Emphasize skills and experience that match the job
            - Use keywords from the job description naturally
            - Maintain professional formatting
            - Keep it concise and relevant
            - Do not add fake information
            """;

        var userPrompt = String.format("""
            Job Title: %s
            Company: %s

            Job Description:
            %s

            Original Resume:
            %s

            Please tailor this resume to match the job description above.
            """, jobTitle, company, jobDescription, resumeText);

        try {
            var tailored = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.3, 2000);
            log.info("Resume tailored for {} at {}", jobTitle, company);
            return Map.of(
                "status", "success",
                "tailoredResume", tailored,
                "jobTitle", jobTitle,
                "company", company
            );
        } catch (Exception e) {
            log.error("Resume tailoring failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String buildProfileSummary(com.jobpilot.domain.agent.CandidateProfile profile) {
        var sb = new StringBuilder();
        if (profile.fullName() != null) sb.append(profile.fullName()).append("\n");
        if (profile.email() != null) sb.append("Email: ").append(profile.email()).append("\n");
        if (profile.phone() != null) sb.append("Phone: ").append(profile.phone()).append("\n");
        if (profile.headline() != null) sb.append(profile.headline()).append("\n");
        if (profile.summary() != null) sb.append("\nSummary:\n").append(profile.summary()).append("\n");
        if (profile.skills() != null && !profile.skills().isEmpty()) {
            sb.append("\nSkills:\n");
            for (var skill : profile.skills()) {
                sb.append("- ").append(skill).append("\n");
            }
        }
        if (profile.experience() != null && !profile.experience().isEmpty()) {
            sb.append("\nExperience:\n");
            for (var exp : profile.experience()) {
                sb.append("- ").append(exp).append("\n");
            }
        }
        if (profile.education() != null && !profile.education().isEmpty()) {
            sb.append("\nEducation:\n");
            for (var edu : profile.education()) {
                sb.append("- ").append(edu).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public int timeoutSeconds() {
        return 120;
    }
}
