package com.jobpilot.application.company.usecase;

import com.jobpilot.application.company.dto.CompanyResponse;
import com.jobpilot.application.company.dto.UpdateCompanyCommand;
import com.jobpilot.application.shared.UseCase;

public interface UpdateCompanyUseCase extends UseCase<UpdateCompanyCommand, CompanyResponse> {}
