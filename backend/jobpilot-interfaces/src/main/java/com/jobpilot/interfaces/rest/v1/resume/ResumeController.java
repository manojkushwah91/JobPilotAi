package com.jobpilot.interfaces.rest.v1.resume;

import com.jobpilot.application.resume.dto.*;
import com.jobpilot.application.resume.usecase.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final CreateResumeUseCase createResumeUseCase;
    private final UpdateResumeUseCase updateResumeUseCase;
    private final DeleteResumeUseCase deleteResumeUseCase;
    private final GetResumeUseCase getResumeUseCase;
    private final ListResumesUseCase listResumesUseCase;

    public ResumeController(CreateResumeUseCase createResumeUseCase,
                            UpdateResumeUseCase updateResumeUseCase,
                            DeleteResumeUseCase deleteResumeUseCase,
                            GetResumeUseCase getResumeUseCase,
                            ListResumesUseCase listResumesUseCase) {
        this.createResumeUseCase = createResumeUseCase;
        this.updateResumeUseCase = updateResumeUseCase;
        this.deleteResumeUseCase = deleteResumeUseCase;
        this.getResumeUseCase = getResumeUseCase;
        this.listResumesUseCase = listResumesUseCase;
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<ResumeResponse>> create(
            @Valid @RequestBody CreateResumeRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new CreateResumeCommand(principal.userId(), request.title(), toSectionDtos(request.sections()));
        var response = createResumeUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeResponse>> getById(@PathVariable String id) {
        var command = new GetResumeCommand(id);
        var response = getResumeUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> list(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new ListResumesCommand(principal.userId());
        var response = listResumesUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateResumeRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new UpdateResumeCommand(id, principal.userId(), request.title(), toSectionDtos(request.sections()));
        var response = updateResumeUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new DeleteResumeCommand(id, principal.userId());
        deleteResumeUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record CreateResumeRequest(
        @NotBlank @Size(max = 255) String title,
        List<SectionRequest> sections
    ) {}

    public record UpdateResumeRequest(
        @NotBlank @Size(max = 255) String title,
        List<SectionRequest> sections
    ) {}

    public record SectionRequest(
        @NotBlank String type,
        String title,
        Map<String, Object> content,
        int sortOrder
    ) {}

    private List<SectionDto> toSectionDtos(List<SectionRequest> sections) {
        if (sections == null) return List.of();
        return sections.stream()
            .map(s -> new SectionDto(null, s.type(), s.title(), s.content(), s.sortOrder()))
            .toList();
    }
}
