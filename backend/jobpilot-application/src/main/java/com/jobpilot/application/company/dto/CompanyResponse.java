package com.jobpilot.application.company.dto;

import com.jobpilot.domain.company.CompanyProfile;
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
) {
    public static CompanyResponse from(CompanyProfile c) {
        return new CompanyResponse(c.companyId().value().toString(), c.name(), c.description(), c.website(),
            c.logoUrl(), c.industry(), c.headquarters(), c.foundedYear(), c.companySizeMin(),
            c.companySizeMax(), c.stockSymbol(), c.fundingRounds(), c.technologyStack(),
            c.cultureKeywords(), c.salaryData(), c.hiringTrends(), c.createdAt(), c.updatedAt());
    }
}
