package com.jobpilot.interfaces.rest.v1.automation;

import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.ConfirmActionCommand;
import com.jobpilot.application.automation.dto.StartAutomationCommand;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.service.AutomationOrchestratorService;
import com.jobpilot.application.automation.usecase.ConfirmAutomationUseCase;
import com.jobpilot.application.automation.usecase.StartAutomationUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.domain.automation.AutomationSessionId;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/automation")
public class AutomationController {

    private final StartAutomationUseCase startAutomationUseCase;
    private final ConfirmAutomationUseCase confirmAutomationUseCase;
    private final AutomationRepository automationRepository;
    private final AutomationOrchestratorService orchestrator;

    public AutomationController(StartAutomationUseCase startAutomationUseCase,
                                 ConfirmAutomationUseCase confirmAutomationUseCase,
                                 AutomationRepository automationRepository,
                                 AutomationOrchestratorService orchestrator) {
        this.startAutomationUseCase = startAutomationUseCase;
        this.confirmAutomationUseCase = confirmAutomationUseCase;
        this.automationRepository = automationRepository;
        this.orchestrator = orchestrator;
    }

    @RateLimited(capacity = 100)
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> startSession(
            @RequestBody StartAutomationRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new StartAutomationCommand(principal.userId(), request.applicationId());
        var response = startAutomationUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Page<AutomationSessionResponse>>> listSessions(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var sessions = automationRepository.findByUserId(UUID.fromString(principal.userId()), PageRequest.of(page, size))
            .map(AutomationSessionResponse::from);
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> getSession(@PathVariable String id) {
        var sessionId = AutomationSessionId.from(UUID.fromString(id));
        var session = automationRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("AutomationSession", id));
        return ResponseEntity.ok(ApiResponse.ok(AutomationSessionResponse.from(session)));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/sessions/{id}/confirm")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> confirmAction(@PathVariable String id) {
        var command = new ConfirmActionCommand(id);
        var response = confirmAutomationUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/sessions/{id}/cancel")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> cancelSession(@PathVariable String id) {
        var sessionId = AutomationSessionId.from(UUID.fromString(id));
        var session = automationRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("AutomationSession", id));
        orchestrator.cancel(session);
        return ResponseEntity.ok(ApiResponse.ok(AutomationSessionResponse.from(session)));
    }

    public record StartAutomationRequest(String applicationId) {}
}
