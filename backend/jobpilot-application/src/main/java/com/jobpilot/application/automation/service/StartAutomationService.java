package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.StartAutomationCommand;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.usecase.StartAutomationUseCase;
import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.automation.AutomationSessionId;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StartAutomationService implements StartAutomationUseCase {

    private final AutomationRepository automationRepository;
    private final AutomationOrchestratorService orchestrator;

    public StartAutomationService(AutomationRepository automationRepository, AutomationOrchestratorService orchestrator) {
        this.automationRepository = automationRepository;
        this.orchestrator = orchestrator;
    }

    @Override
    public AutomationSessionResponse execute(StartAutomationCommand command) {
        var sessionId = AutomationSessionId.generate();
        var userId = UUID.fromString(command.userId());
        var applicationId = UUID.fromString(command.applicationId());
        var session = AutomationSession.start(sessionId, userId, applicationId);
        automationRepository.save(session);
        runAsync(session);
        return AutomationSessionResponse.from(session);
    }

    @Async
    protected void runAsync(AutomationSession session) {
        orchestrator.execute(session);
    }
}
