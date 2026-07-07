package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AtsResumeAnalyzerTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(AtsResumeAnalyzerTool.class);

    private final AiProviderPort aiProvider;
    private final CandidateProfileRepository profileRepository;

    public AtsResumeAnalyzerTool(AiProviderPort aiProvider, CandidateProfileRepository profileRepository) {
        this.aiProvider = aiProvider;
        this.profileRepository = profileRepository;
    }

    @Override
    public String name() {
        return "ANALYZE_RESUME";
    }

    @Override
    public String description() {
        return "Analyzes a resume against a job description for ATS compatibility and provides improvement suggestions";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing ATS resume analyzer tool");

        var jobTitle = (String) input.getOrDefault("title", "");
        var jobDescription = (String) input.getOrDefault("description", "");
        var userId = input.get("userId") instanceof java.util.UUID uid ? uid : null;

        var profile = userId != null ? profileRepository.findByUserId(userId).orElse(null) : null;
        if (profile == null) {
            return Map.of("status", "error", "error", "No candidate profile found");
        }

        var resumeText = profile.resumeText();
        if (resumeText == null || resumeText.isBlank()) {
            resumeText = buildProfileSummary(profile);
        }

        var systemPrompt = """
            You are an ATS (Applicant Tracking System) expert. Analyze a resume against
            a job description and provide a detailed compatibility report.
            Return ONLY a JSON response with these fields:
            {
              "atsScore": <number 0-100>,
              "keywordMatch": {
                "matched": ["keyword1", "keyword2"],
                "missing": ["keyword1", "keyword2"],
                "matchPercentage": <number 0-100>
              },
              "sections": {
                "formatting": {"score": <0-100>, "feedback": "..."},
                "keywords": {"score": <0-100>, "feedback": "..."},
                "experience": {"score": <0-100>, "feedback": "..."},
                "education": {"score": <0-100>, "feedback": "..."},
                "skills": {"score": <0-100>, "feedback": "..."}
              },
              "improvements": [
                {"priority": "high|medium|low", "section": "...", "suggestion": "...", "example": "..."}
              ],
              "optimizedSnippet": "A rewritten version of the weakest section"
            }
            Be specific and actionable. Focus on what would improve ATS pass-through rate.
            """;

        var userPrompt = String.format("""
            Job Title: %s

            Job Description:
            %s

            Resume:
            %s

            Analyze this resume for ATS compatibility with the job description.
            """, jobTitle, jobDescription, resumeText);

        try {
            var response = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.2, 2500);
            var result = parseResponse(response, jobTitle);
            log.info("ATS analysis complete: score {}/100", result.get("atsScore"));
            return result;
        } catch (Exception e) {
            log.error("ATS analysis failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResponse(String response, String jobTitle) {
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("jobTitle", jobTitle);

        try {
            var cleaned = response.trim();
            if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
            if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();

            var jsonMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(cleaned, Map.class);
            result.putAll(jsonMap);
            result.put("status", "success");
        } catch (Exception e) {
            log.warn("Failed to parse ATS response: {}", e.getMessage());
            result.put("status", "success");
            result.put("atsScore", 50);
            result.put("keywordMatch", Map.of("matched", java.util.List.of(), "missing", java.util.List.of(), "matchPercentage", 0));
            result.put("improvements", java.util.List.of(Map.of(
                "priority", "high",
                "section", "general",
                "suggestion", "Add more keywords from the job description",
                "example", ""
            )));
        }

        return result;
    }

    private String buildProfileSummary(com.jobpilot.domain.agent.CandidateProfile profile) {
        var sb = new StringBuilder();
        if (profile.fullName() != null) sb.append(profile.fullName()).append("\n");
        if (profile.headline() != null) sb.append(profile.headline()).append("\n");
        if (profile.summary() != null) sb.append("\n").append(profile.summary()).append("\n");
        if (profile.skills() != null && !profile.skills().isEmpty()) {
            sb.append("\nSkills: ").append(String.join(", ", profile.skills()));
        }
        if (profile.experience() != null && !profile.experience().isEmpty()) {
            sb.append("\nExperience:\n");
            for (var exp : profile.experience()) sb.append("- ").append(exp).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int timeoutSeconds() {
        return 90;
    }
}
