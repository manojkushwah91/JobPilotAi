package com.jobpilot.application.scraper.service;

import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.scraper.dto.ScrapedJobDTO;
import com.jobpilot.application.scraper.ports.JobScraper;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobScrapingService {

    private static final Logger log = LoggerFactory.getLogger(JobScrapingService.class);

    private final List<JobScraper> scrapers;
    private final JobRepository jobRepository;

    public JobScrapingService(List<JobScraper> scrapers, JobRepository jobRepository) {
        this.scrapers = scrapers;
        this.jobRepository = jobRepository;
    }

    public int scrapeAll(String query, String location) {
        int total = 0;
        int skipped = 0;
        int failed = 0;
        for (var scraper : scrapers) {
            if (!scraper.isEnabled()) continue;
            try {
                var results = scraper.scrape(query, location, 50);
                for (var dto : results) {
                    try {
                        if (ingestJob(scraper.sourceName(), dto)) {
                            total++;
                        } else {
                            skipped++;
                        }
                    } catch (Exception e) {
                        failed++;
                        log.warn("Failed to ingest job '{}': {}", dto.title(), e.getMessage());
                    }
                }
                log.info("Scraped {} jobs from {} ({} ingested, {} duplicates, {} failed)",
                    results.size(), scraper.sourceName(), total, skipped, failed);
                skipped = 0;
                failed = 0;
            } catch (Exception e) {
                log.error("Failed to scrape from {}: {}", scraper.sourceName(), e.getMessage());
            }
        }
        return total;
    }

    @Transactional
    boolean ingestJob(String source, ScrapedJobDTO dto) {
        var appUrl = dto.applicationUrl();
        if (appUrl != null && !appUrl.isBlank()) {
            if (jobRepository.findByApplicationUrl(appUrl).isPresent()) {
                return false;
            }
        }

        var jobId = JobId.generate();
        var job = JobListing.create(jobId, source, dto.title(), dto.companyName(), dto.description());

        var employmentType = tryParseEnum(com.jobpilot.domain.job.EmploymentType.class, dto.employmentType());
        var experienceLevel = tryParseEnum(com.jobpilot.domain.job.ExperienceLevel.class, dto.experienceLevel());
        if (experienceLevel == null && dto.experienceLevel() != null) {
            var normalized = normalizeExperienceLevel(dto.experienceLevel());
            if (normalized != null) {
                experienceLevel = com.jobpilot.domain.job.ExperienceLevel.valueOf(normalized);
            }
        }

        job.updateDetails(dto.title(), dto.description(), dto.companyName(),
            dto.location(), dto.salary(),
            employmentType, experienceLevel,
            dto.industry(), dto.skills(), dto.applicationUrl());
        jobRepository.save(job);
        return true;
    }

    private <T extends Enum<T>> T tryParseEnum(Class<T> enumClass, String value) {
        if (value == null) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalizeExperienceLevel(String raw) {
        var upper = raw.toUpperCase().replace("-", "_");
        return switch (upper) {
            case "MID_LEVEL", "MIDLEVEL", "INTERMEDIATE" -> "MID";
            case "JUNIOR", "JUNIOR_LEVEL" -> "ENTRY";
            case "SENIOR_LEVEL", "SR", "SR_LEVEL" -> "SENIOR";
            case "LEAD", "LEAD_LEVEL" -> "LEAD";
            case "EXECUTIVE", "DIRECTOR", "VP", "VP_LEVEL" -> "EXECUTIVE";
            default -> null;
        };
    }
}
