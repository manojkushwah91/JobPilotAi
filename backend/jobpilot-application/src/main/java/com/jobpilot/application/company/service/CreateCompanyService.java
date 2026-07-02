package com.jobpilot.application.company.service;

import com.jobpilot.application.company.dto.CompanyResponse;
import com.jobpilot.application.company.dto.CreateCompanyCommand;
import com.jobpilot.application.company.ports.CompanyRepository;
import com.jobpilot.application.company.usecase.CreateCompanyUseCase;
import com.jobpilot.domain.company.CompanyId;
import com.jobpilot.domain.company.CompanyProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateCompanyService implements CreateCompanyUseCase {

    private final CompanyRepository companyRepository;

    public CreateCompanyService(CompanyRepository companyRepository) { this.companyRepository = companyRepository; }

    @Override
    public CompanyResponse execute(CreateCompanyCommand command) {
        var companyId = CompanyId.generate();
        var company = CompanyProfile.create(companyId, command.name());
        company.updateDetails(command.description(), command.website(), command.logoUrl(),
            command.industry(), command.headquarters(), command.foundedYear(),
            command.companySizeMin(), command.companySizeMax(), command.stockSymbol(),
            command.technologyStack(), command.cultureKeywords());
        companyRepository.save(company);
        return toResponse(company);
    }

    public static CompanyResponse toResponse(CompanyProfile c) {
        return new CompanyResponse(c.companyId().value().toString(), c.name(), c.description(),
            c.website(), c.logoUrl(), c.industry(), c.headquarters(), c.foundedYear(),
            c.companySizeMin(), c.companySizeMax(), c.stockSymbol(), c.fundingRounds(),
            c.technologyStack(), c.cultureKeywords(), c.salaryData(), c.hiringTrends(),
            c.createdAt(), c.updatedAt());
    }
}
