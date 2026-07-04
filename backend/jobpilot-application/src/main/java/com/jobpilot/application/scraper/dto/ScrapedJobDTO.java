package com.jobpilot.application.scraper.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ScrapedJobDTO(
    String sourceId,
    String title,
    String companyName,
    String companyLogoUrl,
    Map<String, Object> location,
    Map<String, Object> salary,
    String description,
    List<String> requirements,
    List<String> responsibilities,
    List<String> benefits,
    String employmentType,
    String experienceLevel,
    String industry,
    List<String> skills,
    String applicationUrl,
    Instant postedAt
) {}
