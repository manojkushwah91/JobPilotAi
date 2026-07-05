package com.jobpilot.application.automation.service;

import com.jobpilot.application.scraper.service.JobScrapingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AutoApplyScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoApplyScheduler.class);

    private final JobScrapingService jobScrapingService;

    @Value("${jobpilot.scraper.search-query:software developer}")
    private String searchQuery;

    @Value("${jobpilot.scraper.search-location:remote}")
    private String searchLocation;

    public AutoApplyScheduler(JobScrapingService jobScrapingService) {
        this.jobScrapingService = jobScrapingService;
    }

    @Scheduled(fixedRateString = "${jobpilot.scraper.schedule-ms:300000}")
    public void scheduledScrape() {
        try {
            var scraped = jobScrapingService.scrapeAll(searchQuery, searchLocation);
            log.info("Scheduled scrape complete: {} new jobs ingested (query='{}', location='{}')",
                scraped, searchQuery, searchLocation);
        } catch (Exception e) {
            log.error("Scheduled scrape failed: {}", e.getMessage());
        }
    }
}
