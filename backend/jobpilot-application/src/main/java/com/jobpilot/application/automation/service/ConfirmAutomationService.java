package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.ConfirmActionCommand;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.usecase.ConfirmAutomationUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.common.exception.ValidationException;
import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.automation.AutomationSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ConfirmAutomationService implements ConfirmAutomationUseCase {

    private final AutomationRepository automationRepository;
    private final AutomationOrchestratorService orchestrator;

    public ConfirmAutomationService(AutomationRepository automationRepository, AutomationOrchestratorService orchestrator) {
        this.automationRepository = automationRepository;
        this.orchestrator = orchestrator;
    }

    @Override
    public AutomationSessionResponse execute(ConfirmActionCommand command) {
        var sessionId = AutomationSessionId.from(UUID.fromString(command.sessionId()));
        var session = automationRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("AutomationSession", command.sessionId()));

        if (session.status() != com.jobpilot.domain.automation.AutomationStatus.AWAITING_CONFIRMATION) {
            throw new ValidationException("session", "Session is not awaiting confirmation");
        }

        orchestrator.confirmAndSubmit(session);
        return AutomationSessionResponse.from(session);
    }
}
