package com.jobpilot.application.company.usecase;

import com.jobpilot.application.company.dto.CompanyResponse;
import com.jobpilot.application.company.dto.GetCompanyCommand;
import com.jobpilot.application.shared.UseCase;

public interface GetCompanyUseCase extends UseCase<GetCompanyCommand, CompanyResponse> {}
