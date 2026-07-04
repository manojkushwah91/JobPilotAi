package com.jobpilot.application.job.service;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.domain.job.JobListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private final JobRepository jobRepository;

    public JobMatchingService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Map<String, Object> analyzeMatch(String jobId, String userId) {
        var jobIdObj = com.jobpilot.domain.job.JobId.from(java.util.UUID.fromString(jobId));
        var job = jobRepository.findById(jobIdObj).orElse(null);
        if (job == null) return Map.of("matchScore", 0, "matchedSkills", List.of(), "missingSkills", List.of());
        return Map.of(
            "matchScore", 75,
            "matchedSkills", job.skills().stream().limit(3).toList(),
            "missingSkills", job.skills().size() > 3 ? job.skills().subList(3, Math.min(job.skills().size(), 6)) : List.of()
        );
    }

    public List<Map<String, Object>> getMatches(String userId) {
        var jobs = jobRepository.findAllActive(PageRequest.of(0, 20));
        return jobs.stream()
            .map(job -> Map.<String, Object>of(
                "id", job.jobId().value().toString(),
                "title", job.title(),
                "companyName", job.companyName(),
                "location", job.location() != null ? job.location().getOrDefault("city", "") : "",
                "salary", job.salary() != null ? job.salary() : Map.of(),
                "employmentType", job.employmentType() != null ? job.employmentType().name() : null,
                "skills", job.skills(),
                "postedAt", job.postedAt() != null ? job.postedAt().toString() : null,
                "matchScore", computeMatchScore(job)
            ))
            .collect(Collectors.toList());
    }

    private int computeMatchScore(JobListing job) {
        var skillCount = job.skills().size();
        if (skillCount == 0) return 50;
        var commonSkills = job.skills().stream()
            .filter(s -> COMMON_SKILLS.contains(s.toLowerCase()))
            .count();
        return (int) Math.min(100, Math.round((double) commonSkills / skillCount * 100));
    }

    private static final Set<String> COMMON_SKILLS = Set.of(
        "java", "python", "javascript", "typescript", "react", "spring", "sql",
        "aws", "docker", "kubernetes", "git", "rest", "api", "css", "html",
        "node.js", "angular", "vue", "mongodb", "postgresql", "redis"
    );
}
