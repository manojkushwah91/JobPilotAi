package com.jobpilot.application.scraper.ports;

import com.jobpilot.application.scraper.dto.ScrapedJobDTO;

import java.util.List;

public interface JobScraper {
    String sourceName();
    List<ScrapedJobDTO> scrape(String query, String location, int maxResults);
    boolean isEnabled();
}
