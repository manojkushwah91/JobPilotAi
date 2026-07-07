package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JobScoringTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(JobScoringTool.class);

    private final AiProviderPort aiProvider;
    private final CandidateProfileRepository profileRepository;

    public JobScoringTool(AiProviderPort aiProvider, CandidateProfileRepository profileRepository) {
        this.aiProvider = aiProvider;
        this.profileRepository = profileRepository;
    }

    @Override
    public String name() {
        return "RANK_JOB";
    }

    @Override
    public String description() {
        return "Scores how well a job matches the user's profile (0-100)";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing job scoring tool");

        var jobTitle = (String) input.getOrDefault("title", "");
        var company = (String) input.getOrDefault("company", "");
        var jobDescription = (String) input.getOrDefault("description", "");
        var userId = input.get("userId") instanceof java.util.UUID uid ? uid : null;

        if (jobDescription.isBlank()) {
            return Map.of("status", "error", "error", "No job description provided");
        }

        var profile = userId != null ? profileRepository.findByUserId(userId).orElse(null) : null;

        var systemPrompt = """
            You are a job matching expert. Score how well a candidate matches a job (0-100).
            Return ONLY a JSON response with these fields:
            {
              "score": <number 0-100>,
              "matchReasons": ["reason1", "reason2"],
              "gapReasons": ["gap1", "gap2"],
              "matchingSkills": ["skill1", "skill2"],
              "missingSkills": ["skill1", "skill2"],
              "recommendation": "apply" | "maybe" | "skip"
            }
            Scoring criteria:
            - Skills match (40%): How many required skills does the candidate have?
            - Experience match (30%): Does the candidate have enough relevant experience?
            - Education match (10%): Does the candidate meet education requirements?
            - Location/Remote match (10%): Is the work arrangement compatible?
            - Salary match (10%): Is the salary range compatible?
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
            Desired Role: %s
            Desired Location: %s
            Work Preference: %s
            Salary Range: %s - %s %s

            Score this candidate for this job.
            """,
            jobTitle, company, jobDescription,
            profile != null ? profile.fullName() : "Unknown",
            profile != null ? profile.headline() : "",
            profile != null ? profile.skills() : "[]",
            profile != null ? profile.yearsExperience() : "0",
            profile != null ? profile.education() : "[]",
            profile != null ? profile.desiredRole() : "",
            profile != null ? profile.desiredLocation() : "",
            profile != null ? profile.workPreference() : "",
            profile != null ? profile.salaryExpectationMin() : "0",
            profile != null ? profile.salaryExpectationMax() : "0",
            profile != null ? profile.currency() : "USD"
        );

        try {
            var response = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.1, 500);
            var result = parseScoreResponse(response, jobTitle, company);
            log.info("Job scored: {} at {} = {}/100", jobTitle, company, result.get("score"));
            return result;
        } catch (Exception e) {
            log.error("Job scoring failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseScoreResponse(String response, String jobTitle, String company) {
        var result = new LinkedHashMap<String, Object>();
        result.put("jobTitle", jobTitle);
        result.put("company", company);

        try {
            var cleaned = response.trim();
            if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
            if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();

            var jsonMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(cleaned, Map.class);
            result.put("score", jsonMap.getOrDefault("score", 50));
            result.put("matchReasons", jsonMap.getOrDefault("matchReasons", java.util.List.of()));
            result.put("gapReasons", jsonMap.getOrDefault("gapReasons", java.util.List.of()));
            result.put("matchingSkills", jsonMap.getOrDefault("matchingSkills", java.util.List.of()));
            result.put("missingSkills", jsonMap.getOrDefault("missingSkills", java.util.List.of()));
            result.put("recommendation", jsonMap.getOrDefault("recommendation", "maybe"));
            result.put("status", "success");
        } catch (Exception e) {
            log.warn("Failed to parse AI score response, using fallback: {}", e.getMessage());
            int score = extractFallbackScore(response);
            result.put("score", score);
            result.put("recommendation", score >= 70 ? "apply" : score >= 40 ? "maybe" : "skip");
            result.put("matchReasons", java.util.List.of("Parsed from AI response"));
            result.put("gapReasons", java.util.List.of());
            result.put("matchingSkills", java.util.List.of());
            result.put("missingSkills", java.util.List.of());
            result.put("status", "success");
        }

        return result;
    }

    private int extractFallbackScore(String response) {
        try {
            var pattern = java.util.regex.Pattern.compile("(\\d{1,3})");
            var matcher = pattern.matcher(response);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception ignored) {}
        return 50;
    }

    @Override
    public int timeoutSeconds() {
        return 60;
    }
}
