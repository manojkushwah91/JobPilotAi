package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.StartAutomationCommand;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.usecase.StartAutomationUseCase;
import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.automation.AutomationSessionId;
import com.jobpilot.domain.application.ApplicationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AutomationSessionService implements StartAutomationUseCase {

    private static final Logger log = LoggerFactory.getLogger(AutomationSessionService.class);

    private final AutomationRepository automationRepository;
    private final ApplicationRepository applicationRepository;

    public AutomationSessionService(AutomationRepository automationRepository,
                                     ApplicationRepository applicationRepository) {
        this.automationRepository = automationRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    @Transactional
    public AutomationSessionResponse execute(StartAutomationCommand command) {
        var sessionId = AutomationSessionId.generate();
        applicationRepository.findById(ApplicationId.from(UUID.fromString(command.applicationId())))
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + command.applicationId()));

        var session = AutomationSession.start(
            sessionId, UUID.fromString(command.userId()), UUID.fromString(command.applicationId()));
        automationRepository.save(session);
        log.info("Started automation session {} for application {}", sessionId.value(), command.applicationId());
        return AutomationSessionResponse.from(session);
    }
}
