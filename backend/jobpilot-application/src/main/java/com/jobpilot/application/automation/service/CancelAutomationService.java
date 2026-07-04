package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.usecase.CancelAutomationUseCase;
import com.jobpilot.domain.automation.AutomationSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CancelAutomationService implements CancelAutomationUseCase {

    private final AutomationRepository automationRepository;

    public CancelAutomationService(AutomationRepository automationRepository) {
        this.automationRepository = automationRepository;
    }

    @Override
    @Transactional
    public AutomationSessionResponse execute(String sessionId) {
        var id = AutomationSessionId.from(UUID.fromString(sessionId));
        var session = automationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        session.cancel();
        automationRepository.save(session);
        return AutomationSessionResponse.from(session);
    }
}
