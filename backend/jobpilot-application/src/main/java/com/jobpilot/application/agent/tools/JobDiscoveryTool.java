package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JobDiscoveryTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(JobDiscoveryTool.class);

    private final AiProviderPort aiProvider;

    public JobDiscoveryTool(AiProviderPort aiProvider) {
        this.aiProvider = aiProvider;
    }

    @Override
    public String name() {
        return "DISCOVER_JOBS";
    }

    @Override
    public String description() {
        return "Searches for jobs matching the mission criteria from multiple sources";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing job discovery tool");

        var query = (String) input.getOrDefault("query", "");
        var location = (String) input.getOrDefault("location", "");
        var skills = (String) input.getOrDefault("skills", "");

        var systemPrompt = "You are a job discovery assistant. Given the search criteria, " +
            "generate a structured list of job opportunities. Return a JSON array of job objects " +
            "with fields: title, company, location, salary, url, matchScore.";

        var userPrompt = String.format(
            "Search for jobs with criteria:\nQuery: %s\nLocation: %s\nSkills: %s",
            query, location, skills
        );

        var result = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.3, 2000);

        return Map.of(
            "status", "success",
            "jobsFound", result,
            "query", query,
            "location", location
        );
    }

    @Override
    public int timeoutSeconds() {
        return 60;
    }
}
