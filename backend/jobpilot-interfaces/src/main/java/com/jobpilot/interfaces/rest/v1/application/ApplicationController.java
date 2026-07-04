package com.jobpilot.interfaces.rest.v1.application;

import com.jobpilot.application.application.dto.*;
import com.jobpilot.application.application.ports.ApplicationQueryRepository;
import com.jobpilot.application.application.usecase.*;
import com.jobpilot.application.automation.dto.AutomationSessionResponse;
import com.jobpilot.application.automation.dto.StartAutomationCommand;
import com.jobpilot.application.automation.usecase.StartAutomationUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplyUseCase applyUseCase;
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;
    private final GetApplicationUseCase getApplicationUseCase;
    private final ListApplicationsUseCase listApplicationsUseCase;
    private final DeleteApplicationUseCase deleteApplicationUseCase;
    private final StartAutomationUseCase startAutomationUseCase;
    private final ApplicationQueryRepository applicationQueryRepository;

    public ApplicationController(ApplyUseCase applyUseCase,
                                  UpdateApplicationStatusUseCase updateApplicationStatusUseCase,
                                  GetApplicationUseCase getApplicationUseCase,
                                  ListApplicationsUseCase listApplicationsUseCase,
                                  DeleteApplicationUseCase deleteApplicationUseCase,
                                  StartAutomationUseCase startAutomationUseCase,
                                  ApplicationQueryRepository applicationQueryRepository) {
        this.applyUseCase = applyUseCase;
        this.updateApplicationStatusUseCase = updateApplicationStatusUseCase;
        this.getApplicationUseCase = getApplicationUseCase;
        this.listApplicationsUseCase = listApplicationsUseCase;
        this.deleteApplicationUseCase = deleteApplicationUseCase;
        this.startAutomationUseCase = startAutomationUseCase;
        this.applicationQueryRepository = applicationQueryRepository;
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @RequestBody ApplyRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new ApplyCommand(principal.userId(), request.jobListingId(), request.resumeId());
        var response = applyUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> list(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var response = listApplicationsUseCase.execute(new ListApplicationsCommand(principal.userId()));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getById(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var response = getApplicationUseCase.execute(new GetApplicationCommand(id));
        if (!response.userId().equals(principal.userId())) throw new UnauthorizedException("Access denied");
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PatchMapping("/{id}/status")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var response = updateApplicationStatusUseCase.execute(
            new UpdateApplicationStatusCommand(id, request.status()));
        if (!response.userId().equals(principal.userId())) throw new UnauthorizedException("Access denied");
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var response = getApplicationUseCase.execute(new GetApplicationCommand(id));
        if (!response.userId().equals(principal.userId())) throw new UnauthorizedException("Access denied");
        deleteApplicationUseCase.execute(new DeleteApplicationCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/{id}/automate")
    public ResponseEntity<ApiResponse<AutomationSessionResponse>> automate(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var response = startAutomationUseCase.execute(
            new StartAutomationCommand(principal.userId(), id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addNote(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var response = applicationQueryRepository.addNote(
            UUID.fromString(id), UUID.fromString(principal.userId()),
            body.getOrDefault("content", ""));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @DeleteMapping("/{id}/notes/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable String id,
            @PathVariable String noteId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        applicationQueryRepository.deleteNote(UUID.fromString(id), UUID.fromString(noteId));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotes(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var notes = applicationQueryRepository.findNotesByApplicationId(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.ok(notes));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAttachments(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var attachments = applicationQueryRepository.findAttachmentsByApplicationId(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.ok(attachments));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/follow-ups")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFollowUps(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var followups = applicationQueryRepository.findFollowUpsByApplicationId(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.ok(followups));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTimeline(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var timeline = applicationQueryRepository.findTimelineByApplicationId(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.ok(timeline));
    }

    public record ApplyRequest(String jobListingId, String resumeId) {}
    public record UpdateStatusRequest(String status) {}
}
