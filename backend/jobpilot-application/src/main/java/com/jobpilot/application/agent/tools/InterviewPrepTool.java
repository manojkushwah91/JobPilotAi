package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InterviewPrepTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(InterviewPrepTool.class);

    private final AiProviderPort aiProvider;
    private final CandidateProfileRepository profileRepository;

    public InterviewPrepTool(AiProviderPort aiProvider, CandidateProfileRepository profileRepository) {
        this.aiProvider = aiProvider;
        this.profileRepository = profileRepository;
    }

    @Override
    public String name() {
        return "PREPARE_INTERVIEW";
    }

    @Override
    public String description() {
        return "Generates interview questions, answers, and preparation strategy for a specific job";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing interview prep tool");

        var jobTitle = (String) input.getOrDefault("title", "");
        var company = (String) input.getOrDefault("company", "");
        var jobDescription = (String) input.getOrDefault("description", "");
        var userId = input.get("userId") instanceof java.util.UUID uid ? uid : null;

        if (jobDescription.isBlank()) {
            return Map.of("status", "error", "error", "No job description provided");
        }

        var profile = userId != null ? profileRepository.findByUserId(userId).orElse(null) : null;

        var systemPrompt = """
            You are an expert interview coach. Generate a comprehensive interview preparation
            package for a candidate. Return ONLY a JSON response with these fields:
            {
              "technicalQuestions": [
                {"question": "...", "idealAnswer": "...", "tips": "..."}
              ],
              "behavioralQuestions": [
                {"question": "...", "idealAnswer": "...", "starMethod": "Situation:... Task:... Action:... Result:..."}
              ],
              "companyQuestions": [
                {"question": "...", "whyAsked": "...", "howToResearch": "..."}
              ],
              "strengths": ["..."],
              "weaknesses": ["..."],
              "strategy": {
                "beforeInterview": ["..."],
                "duringInterview": ["..."],
                "afterInterview": ["..."]
              },
              "salaryNegotiation": {
                "researchTips": ["..."],
                "negotiationScript": "..."
              }
            }
            Generate 5 technical, 5 behavioral, and 3 company-specific questions.
            Tailor everything to the specific role and company.
            """;

        var userPrompt = String.format("""
            Job Title: %s
            Company: %s

            Job Description:
            %s

            Candidate Profile:
            Name: %s
            Headline: %s
            Skills: %s
            Years Experience: %s
            Education: %s

            Generate a complete interview preparation package.
            """,
            jobTitle, company, jobDescription,
            profile != null ? profile.fullName() : "Unknown",
            profile != null ? profile.headline() : "",
            profile != null ? profile.skills() : "[]",
            profile != null ? profile.yearsExperience() : "0",
            profile != null ? profile.education() : "[]"
        );

        try {
            var response = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.3, 3000);
            var result = parseResponse(response, jobTitle, company);
            log.info("Interview prep generated for {} at {}", jobTitle, company);
            return result;
        } catch (Exception e) {
            log.error("Interview prep failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResponse(String response, String jobTitle, String company) {
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("jobTitle", jobTitle);
        result.put("company", company);

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
            log.warn("Failed to parse interview prep response: {}", e.getMessage());
            result.put("status", "success");
            result.put("rawResponse", response);
            result.put("technicalQuestions", java.util.List.of());
            result.put("behavioralQuestions", java.util.List.of());
            result.put("companyQuestions", java.util.List.of());
            result.put("strategy", Map.of(
                "beforeInterview", java.util.List.of("Research the company", "Review job description"),
                "duringInterview", java.util.List.of("Be specific with examples", "Ask thoughtful questions"),
                "afterInterview", java.util.List.of("Send thank you email", "Follow up within a week")
            ));
        }

        return result;
    }

    @Override
    public int timeoutSeconds() {
        return 120;
    }
}
