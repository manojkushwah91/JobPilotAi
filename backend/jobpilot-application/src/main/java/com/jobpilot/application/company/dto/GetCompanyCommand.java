package com.jobpilot.application.company.dto;

import com.jobpilot.common.exception.ValidationException;

public record GetCompanyCommand(String companyId) {
    public GetCompanyCommand {
        if (companyId == null || companyId.isBlank()) throw new ValidationException("companyId", "Company ID must not be blank");
    }
}
