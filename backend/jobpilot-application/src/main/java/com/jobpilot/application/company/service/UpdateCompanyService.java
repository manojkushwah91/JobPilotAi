package com.jobpilot.application.company.service;

import com.jobpilot.application.company.dto.CompanyResponse;
import com.jobpilot.application.company.dto.UpdateCompanyCommand;
import com.jobpilot.application.company.ports.CompanyRepository;
import com.jobpilot.application.company.usecase.UpdateCompanyUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.company.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateCompanyService implements UpdateCompanyUseCase {

    private final CompanyRepository companyRepository;

    public UpdateCompanyService(CompanyRepository companyRepository) { this.companyRepository = companyRepository; }

    @Override
    public CompanyResponse execute(UpdateCompanyCommand command) {
        var companyId = CompanyId.from(UUID.fromString(command.companyId()));
        var company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("CompanyProfile", command.companyId()));
        company.updateDetails(command.description(), command.website(), command.logoUrl(),
            command.industry(), command.headquarters(), command.foundedYear(),
            command.companySizeMin(), command.companySizeMax(), command.stockSymbol(),
            command.technologyStack(), command.cultureKeywords());
        companyRepository.save(company);
        return CreateCompanyService.toResponse(company);
    }
}
