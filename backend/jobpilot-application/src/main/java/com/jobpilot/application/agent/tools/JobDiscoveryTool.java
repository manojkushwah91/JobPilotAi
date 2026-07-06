package com.jobpilot.application.agent.tools;

import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.domain.agent.Tool;
import com.jobpilot.domain.job.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JobDiscoveryTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(JobDiscoveryTool.class);

    private final JobRepository jobRepository;

    public JobDiscoveryTool(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public String name() {
        return "DISCOVER_JOBS";
    }

    @Override
    public String description() {
        return "Searches for jobs matching the mission criteria from the scraped job database";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing job discovery tool with input: {}", input);

        var query = (String) input.getOrDefault("query", "");
        var location = (String) input.getOrDefault("location", "");
        var skills = input.getOrDefault("skills", "");
        var limit = input.containsKey("limit") ? ((Number) input.get("limit")).intValue() : 20;

        try {
            var searchQuery = buildSearchQuery(query, skills);
            var jobs = jobRepository.search(searchQuery, PageRequest.of(0, limit));

            var jobList = new ArrayList<Map<String, Object>>();
            for (var job : jobs.getContent()) {
                jobList.add(toMap(job));
            }

            log.info("Discovered {} jobs for query '{}'", jobList.size(), searchQuery);

            return Map.of(
                "status", "success",
                "jobsFound", jobList,
                "totalFound", jobs.getTotalElements(),
                "query", searchQuery,
                "location", location != null ? location : ""
            );
        } catch (Exception e) {
            log.error("Job discovery failed: {}", e.getMessage(), e);
            return Map.of(
                "status", "error",
                "error", e.getMessage(),
                "jobsFound", List.of()
            );
        }
    }

    private String buildSearchQuery(String query, Object skills) {
        var sb = new StringBuilder();
        if (query != null && !query.isBlank()) {
            sb.append(query);
        }
        if (skills instanceof String s && !s.isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(s);
        }
        return sb.isEmpty() ? null : sb.toString().trim();
    }

    private Map<String, Object> toMap(JobListing job) {
        var map = new LinkedHashMap<String, Object>();
        map.put("id", job.jobId().value().toString());
        map.put("title", job.title());
        map.put("company", job.companyName());
        map.put("location", job.location() != null ? job.location().get("text") : null);
        map.put("url", job.applicationUrl());
        map.put("description", job.description() != null ?
            job.description().substring(0, Math.min(500, job.description().length())) : "");
        map.put("employmentType", job.employmentType() != null ? job.employmentType().name() : null);
        map.put("experienceLevel", job.experienceLevel() != null ? job.experienceLevel().name() : null);
        map.put("skills", job.skills() != null ? job.skills() : List.of());
        map.put("postedAt", job.postedAt() != null ? job.postedAt().toString() : null);
        return map;
    }

    @Override
    public int timeoutSeconds() {
        return 30;
    }
}
