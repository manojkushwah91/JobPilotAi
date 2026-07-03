package com.jobpilot.interfaces.rest.v1.application;

import com.jobpilot.application.application.dto.*;
import com.jobpilot.application.application.usecase.*;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplyUseCase applyUseCase;
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;
    private final GetApplicationUseCase getApplicationUseCase;
    private final ListApplicationsUseCase listApplicationsUseCase;
    private final DeleteApplicationUseCase deleteApplicationUseCase;

    public ApplicationController(ApplyUseCase applyUseCase,
                                  UpdateApplicationStatusUseCase updateApplicationStatusUseCase,
                                  GetApplicationUseCase getApplicationUseCase,
                                  ListApplicationsUseCase listApplicationsUseCase,
                                  DeleteApplicationUseCase deleteApplicationUseCase) {
        this.applyUseCase = applyUseCase;
        this.updateApplicationStatusUseCase = updateApplicationStatusUseCase;
        this.getApplicationUseCase = getApplicationUseCase;
        this.listApplicationsUseCase = listApplicationsUseCase;
        this.deleteApplicationUseCase = deleteApplicationUseCase;
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

    public record ApplyRequest(String jobListingId, String resumeId) {}
    public record UpdateStatusRequest(String status) {}
}
