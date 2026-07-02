package com.jobpilot.application.application.service;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.GetApplicationCommand;
import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.application.application.usecase.GetApplicationUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.application.ApplicationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetApplicationService implements GetApplicationUseCase {

    private final ApplicationRepository applicationRepository;

    public GetApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public ApplicationResponse execute(GetApplicationCommand command) {
        var appId = ApplicationId.from(UUID.fromString(command.applicationId()));
        var app = applicationRepository.findById(appId)
            .orElseThrow(() -> new NotFoundException("Application", command.applicationId()));
        return ApplyService.toResponse(app);
    }
}
