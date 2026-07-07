package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.domain.agent.Tool;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JobScrapingTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(JobScrapingTool.class);

    private final BrowserAutomationPort browserAutomation;
    private final JobRepository jobRepository;

    public JobScrapingTool(BrowserAutomationPort browserAutomation, JobRepository jobRepository) {
        this.browserAutomation = browserAutomation;
        this.jobRepository = jobRepository;
    }

    @Override
    public String name() {
        return "SCRAPE_JOBS";
    }

    @Override
    public String description() {
        return "Scrapes jobs from Indeed or LinkedIn with full descriptions for AI scoring";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing job scraping tool");

        var query = (String) input.getOrDefault("query", "software engineer");
        var location = (String) input.getOrDefault("location", "");
        var board = (String) input.getOrDefault("board", "indeed");
        var maxJobs = input.containsKey("maxJobs") ? ((Number) input.get("maxJobs")).intValue() : 10;

        try {
            browserAutomation.launchBrowser();

            var searchUrl = buildSearchUrl(board, query, location);
            log.info("Scraping {} jobs from: {}", board, searchUrl);

            browserAutomation.navigateTo(searchUrl);
            Thread.sleep(3000);

            var pageContent = browserAutomation.getPageContent();
            var jobs = extractJobsFromPage(pageContent, board);

            var savedCount = 0;
            var skippedCount = 0;
            var savedJobs = new ArrayList<Map<String, Object>>();

            for (var job : jobs) {
                if (savedCount >= maxJobs) break;

                var url = (String) job.get("url");
                if (url == null || url.isBlank()) continue;

                var existing = jobRepository.findByApplicationUrl(url);
                if (existing.isPresent()) {
                    skippedCount++;
                    continue;
                }

                try {
                    var description = scrapeJobDescription(url);
                    var listing = JobListing.reconstitute(
                        JobId.generate(),
                        board,
                        null,
                        (String) job.get("title"),
                        (String) job.get("company"),
                        null,
                        null,
                        job.get("location") != null ? Map.of("text", job.get("location")) : null,
                        null,
                        description != null ? description : (String) job.getOrDefault("description", ""),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        url,
                        null,
                        true,
                        java.time.Instant.now(),
                        java.time.Instant.now()
                    );

                    jobRepository.save(listing);
                    savedCount++;
                    savedJobs.add(job);
                    log.info("Saved job: {} at {} ({})", job.get("title"), job.get("company"), board);
                } catch (Exception e) {
                    log.warn("Failed to save job {}: {}", job.get("title"), e.getMessage());
                }
            }

            browserAutomation.closeBrowser();

            log.info("Scraping complete: {} saved, {} skipped (duplicates), {} total found",
                savedCount, skippedCount, jobs.size());

            return Map.of(
                "status", "success",
                "board", board,
                "query", query,
                "totalFound", jobs.size(),
                "saved", savedCount,
                "skipped", skippedCount,
                "jobs", savedJobs
            );

        } catch (Exception e) {
            log.error("Job scraping failed: {}", e.getMessage());
            try { browserAutomation.closeBrowser(); } catch (Exception ignored) {}
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    private String buildSearchUrl(String board, String query, String location) {
        var encodedQuery = query.replace(" ", "+");
        var encodedLocation = location != null && !location.isBlank() ? location.replace(" ", "+") : "";

        return switch (board.toLowerCase()) {
            case "indeed" -> "https://www.indeed.com/jobs?q=" + encodedQuery
                + (!encodedLocation.isBlank() ? "&l=" + encodedLocation : "");
            case "linkedin" -> "https://www.linkedin.com/jobs/search/?keywords=" + encodedQuery
                + (!encodedLocation.isBlank() ? "&location=" + encodedLocation : "");
            default -> "https://www.indeed.com/jobs?q=" + encodedQuery;
        };
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractJobsFromPage(String html, String board) {
        var jobs = new ArrayList<Map<String, Object>>();

        var titlePattern = Pattern.compile(
            board.equalsIgnoreCase("linkedin")
                ? "<a[^>]*class=\"[^\"]*job-card[^\"]*\"[^>]*href=\"([^\"]+)\"[^>]*>.*?<h3[^>]*>([^<]+)</h3>.*?<span[^>]*class=\"[^\"]*company[^\"]*\"[^>]*>([^<]+)</span>"
                : "<h2[^>]*class=\"[^\"]*jobTitle[^\"]*\"[^>]*>.*?<a[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>.*?<span[^>]*class=\"[^\"]*companyName[^\"]*\"[^>]*>([^<]+)</span>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );

        var matcher = titlePattern.matcher(html);
        while (matcher.find() && jobs.size() < 25) {
            var url = matcher.group(1);
            var title = matcher.group(2).trim();
            var company = matcher.group(3).trim();

            if (url != null && !url.startsWith("http")) {
                url = board.equalsIgnoreCase("linkedin") ? "https://www.linkedin.com" + url : "https://www.indeed.com" + url;
            }

            jobs.add(Map.of(
                "title", title,
                "company", company,
                "url", url
            ));
        }

        if (jobs.isEmpty()) {
            var simplePattern = Pattern.compile(
                "<a[^>]*href=\"([^\"]*(?:viewjob|job)[^\"]+)\"[^>]*>([^<]{10,80})</a>",
                Pattern.CASE_INSENSITIVE
            );
            var simpleMatcher = simplePattern.matcher(html);
            while (simpleMatcher.find() && jobs.size() < 25) {
                var url = simpleMatcher.group(1);
                var title = simpleMatcher.group(2).trim();
                if (url != null && !url.startsWith("http")) {
                    url = "https://www." + board.toLowerCase() + ".com" + url;
                }
                jobs.add(Map.of("title", title, "company", "Unknown", "url", url));
            }
        }

        return jobs;
    }

    private String scrapeJobDescription(String jobUrl) {
        try {
            browserAutomation.navigateTo(jobUrl);
            Thread.sleep(2000);

            var content = browserAutomation.getPageContent();

            var descPattern = Pattern.compile(
                "<div[^>]*class=\"[^\"]*(?:jobsearch-JobComponent-description|job-description|description)[^\"]*\"[^>]*>(.*?)</div>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
            );
            var matcher = descPattern.matcher(content);
            if (matcher.find()) {
                var desc = matcher.group(1);
                desc = desc.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                return desc.length() > 5000 ? desc.substring(0, 5000) : desc;
            }

            var bodyPattern = Pattern.compile("<body[^>]*>(.*?)</body>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            var bodyMatcher = bodyPattern.matcher(content);
            if (bodyMatcher.find()) {
                var body = bodyMatcher.group(1);
                body = body.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                return body.length() > 3000 ? body.substring(0, 3000) : body;
            }
        } catch (Exception e) {
            log.warn("Failed to scrape description from {}: {}", jobUrl, e.getMessage());
        }
        return null;
    }

    @Override
    public int timeoutSeconds() {
        return 180;
    }
}
