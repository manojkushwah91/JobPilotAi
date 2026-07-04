package com.jobpilot.application.automation.service;

import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.scraper.service.JobScrapingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AutoApplyScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoApplyScheduler.class);

    private final JobScrapingService jobScrapingService;
    private final JobRepository jobRepository;

    public AutoApplyScheduler(JobScrapingService jobScrapingService,
                              JobRepository jobRepository) {
        this.jobScrapingService = jobScrapingService;
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void scheduledScrape() {
        var scraped = jobScrapingService.scrapeAll("software developer", "remote");
        log.info("Scheduled scrape complete: {} jobs ingested", scraped);
    }
}