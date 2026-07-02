package com.jobpilot.application.company.usecase;

import com.jobpilot.application.company.dto.CompanyResponse;
import com.jobpilot.application.company.dto.CreateCompanyCommand;
import com.jobpilot.application.shared.UseCase;

public interface CreateCompanyUseCase extends UseCase<CreateCompanyCommand, CompanyResponse> {}
