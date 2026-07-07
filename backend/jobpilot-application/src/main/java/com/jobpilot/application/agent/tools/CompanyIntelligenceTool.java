package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CompanyIntelligenceTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(CompanyIntelligenceTool.class);

    private final AiProviderPort aiProvider;

    public CompanyIntelligenceTool(AiProviderPort aiProvider) {
        this.aiProvider = aiProvider;
    }

    @Override
    public String name() {
        return "RESEARCH_COMPANY";
    }

    @Override
    public String description() {
        return "Researches a company and provides intelligence for interview preparation";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing company intelligence tool");

        var company = (String) input.getOrDefault("company", "");
        var jobTitle = (String) input.getOrDefault("title", "");

        if (company.isBlank()) {
            return Map.of("status", "error", "error", "No company name provided");
        }

        var systemPrompt = """
            You are a corporate intelligence analyst. Research and provide a comprehensive
            company briefing for a job candidate. Return ONLY a JSON response:
            {
              "overview": {
                "description": "Brief company description",
                "industry": "...",
                "founded": "...",
                "headquarters": "...",
                "size": "...",
                "type": "public|private|startup|nonprofit"
              },
              "culture": {
                "values": ["..."],
                "workEnvironment": "...",
                "managementStyle": "...",
                "remotePolicy": "..."
              },
              "financials": {
                "revenue": "...",
                "growth": "...",
                "fundingStage": "...",
                "recentNews": "..."
              },
              "interviewInsights": {
                "process": ["step1", "step2"],
                "difficulty": "easy|medium|hard",
                "commonQuestions": ["..."],
                "whatTheyValue": ["..."]
              },
              "competitors": ["..."],
              "pros": ["..."],
              "cons": ["..."],
              "talkingPoints": ["..."],
              "redFlags": ["..."]
            }
            Be factual and balanced. Note that some information may be general knowledge.
            """;

        var userPrompt = String.format("""
            Company: %s
            Position: %s

            Provide a comprehensive company intelligence briefing for a candidate
            interviewing at this company for the %s role.
            """, company, jobTitle, jobTitle);

        try {
            var response = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.3, 2000);
            var result = parseResponse(response, company, jobTitle);
            log.info("Company intelligence generated for {}", company);
            return result;
        } catch (Exception e) {
            log.error("Company research failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResponse(String response, String company, String jobTitle) {
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("company", company);
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
            log.warn("Failed to parse company research response: {}", e.getMessage());
            result.put("status", "success");
            result.put("overview", Map.of("description", "Research " + company + " on LinkedIn and their website"));
            result.put("talkingPoints", java.util.List.of(
                "Research recent company news",
                "Understand their products/services",
                "Know their competitors",
                "Review the company culture page"
            ));
            result.put("pros", java.util.List.of());
            result.put("cons", java.util.List.of());
            result.put("redFlags", java.util.List.of());
        }

        return result;
    }

    @Override
    public int timeoutSeconds() {
        return 60;
    }
}
