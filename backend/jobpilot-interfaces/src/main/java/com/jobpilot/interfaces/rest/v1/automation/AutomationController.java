package com.jobpilot.interfaces.rest.v1.automation;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.ConfirmActionCommand;
import com.jobpilot.application.automation.dto.StartAutomationCommand;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.usecase.CancelAutomationUseCase;
import com.jobpilot.application.automation.usecase.ConfirmAutomationUseCase;
import com.jobpilot.application.automation.usecase.StartAutomationUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.domain.automation.AutomationSessionId;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/automation")
public class AutomationController {

    private final StartAutomationUseCase startAutomationUseCase;
    private final ConfirmAutomationUseCase confirmAutomationUseCase;
    private final CancelAutomationUseCase cancelAutomationUseCase;
    private final AutomationRepository automationRepository;

    public AutomationController(StartAutomationUseCase startAutomationUseCase,
                                 ConfirmAutomationUseCase confirmAutomationUseCase,
                                 CancelAutomationUseCase cancelAutomationUseCase,
                                 AutomationRepository automationRepository) {
        this.startAutomationUseCase = startAutomationUseCase;
        this.confirmAutomationUseCase = confirmAutomationUseCase;
        this.cancelAutomationUseCase = cancelAutomationUseCase;
        this.automationRepository = automationRepository;
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> getSession(@PathVariable String id) {
        var sessionId = AutomationSessionId.from(UUID.fromString(id));
        var session = automationRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(AutomationSessionResponse.from(session)));
    }

    @PostMapping("/sessions/{id}/confirm")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> confirm(@PathVariable String id) {
        var response = confirmAutomationUseCase.execute(new ConfirmActionCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/sessions/{id}/cancel")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> cancel(@PathVariable String id) {
        var response = cancelAutomationUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
