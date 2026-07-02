package com.jobpilot.application.application.service;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.UpdateApplicationStatusCommand;
import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.application.application.usecase.UpdateApplicationStatusUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.application.ApplicationId;
import com.jobpilot.domain.application.ApplicationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateApplicationStatusService implements UpdateApplicationStatusUseCase {

    private final ApplicationRepository applicationRepository;

    public UpdateApplicationStatusService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public ApplicationResponse execute(UpdateApplicationStatusCommand command) {
        var appId = ApplicationId.from(UUID.fromString(command.applicationId()));
        var app = applicationRepository.findById(appId)
            .orElseThrow(() -> new NotFoundException("Application", command.applicationId()));

        app.updateStatus(ApplicationStatus.valueOf(command.status()));
        applicationRepository.save(app);
        return ApplyService.toResponse(app);
    }
}
