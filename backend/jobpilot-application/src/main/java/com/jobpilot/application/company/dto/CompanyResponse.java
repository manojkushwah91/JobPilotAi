package com.jobpilot.application.company.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CompanyResponse(
    String id, String name, String description, String website, String logoUrl,
    String industry, Map<String, Object> headquarters, Integer foundedYear,
    Integer companySizeMin, Integer companySizeMax, String stockSymbol,
    List<Map<String, Object>> fundingRounds, List<String> technologyStack,
    List<String> cultureKeywords, Map<String, Object> salaryData,
    Map<String, Object> hiringTrends, Instant createdAt, Instant updatedAt
) {}
