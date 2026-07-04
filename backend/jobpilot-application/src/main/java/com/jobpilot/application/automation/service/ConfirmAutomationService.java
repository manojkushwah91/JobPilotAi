package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.ConfirmActionCommand;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.usecase.ConfirmAutomationUseCase;
import com.jobpilot.domain.automation.AutomationSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConfirmAutomationService implements ConfirmAutomationUseCase {

    private final AutomationRepository automationRepository;

    public ConfirmAutomationService(AutomationRepository automationRepository) {
        this.automationRepository = automationRepository;
    }

    @Override
    @Transactional
    public AutomationSessionResponse execute(ConfirmActionCommand command) {
        var sessionId = AutomationSessionId.from(UUID.fromString(command.sessionId()));
        var session = automationRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + command.sessionId()));
        session.confirm();
        automationRepository.save(session);
        return AutomationSessionResponse.from(session);
    }
}
