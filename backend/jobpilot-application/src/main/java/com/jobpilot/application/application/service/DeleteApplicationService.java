package com.jobpilot.application.application.service;

import com.jobpilot.application.application.dto.DeleteApplicationCommand;
import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.application.application.usecase.DeleteApplicationUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.application.ApplicationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteApplicationService implements DeleteApplicationUseCase {

    private final ApplicationRepository applicationRepository;

    public DeleteApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Void execute(DeleteApplicationCommand command) {
        var appId = ApplicationId.from(UUID.fromString(command.applicationId()));
        var app = applicationRepository.findById(appId)
            .orElseThrow(() -> new NotFoundException("Application", command.applicationId()));
        app.softDelete();
        applicationRepository.save(app);
        return null;
    }
}
