package com.jobpilot.application.company.dto;

import com.jobpilot.common.exception.ValidationException;

import java.util.List;
import java.util.Map;

public record UpdateCompanyCommand(
    String companyId, String name, String description, String website, String logoUrl,
    String industry, Map<String, Object> headquarters, Integer foundedYear,
    Integer companySizeMin, Integer companySizeMax, String stockSymbol,
    List<String> technologyStack, List<String> cultureKeywords
) {
    public UpdateCompanyCommand {
        if (companyId == null || companyId.isBlank()) throw new ValidationException("companyId", "Company ID must not be blank");
        if (name == null || name.isBlank()) throw new ValidationException("name", "Name must not be blank");
    }
}
