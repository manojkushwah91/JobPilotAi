package com.jobpilot.application.company.service;

import com.jobpilot.application.company.dto.CompanyResponse;
import com.jobpilot.application.company.dto.GetCompanyCommand;
import com.jobpilot.application.company.ports.CompanyRepository;
import com.jobpilot.application.company.usecase.GetCompanyUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.company.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetCompanyService implements GetCompanyUseCase {

    private final CompanyRepository companyRepository;

    public GetCompanyService(CompanyRepository companyRepository) { this.companyRepository = companyRepository; }

    @Override
    public CompanyResponse execute(GetCompanyCommand command) {
        var companyId = CompanyId.from(UUID.fromString(command.companyId()));
        var company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("CompanyProfile", command.companyId()));
        return CreateCompanyService.toResponse(company);
    }
}
