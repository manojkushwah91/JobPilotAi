package com.jobpilot.interfaces.rest.v1.coverletter;

import com.jobpilot.application.coverletter.dto.CoverLetterResponse;
import com.jobpilot.application.coverletter.dto.CreateCoverLetterCommand;
import com.jobpilot.application.coverletter.dto.GenerateCoverLetterCommand;
import com.jobpilot.application.coverletter.ports.CoverLetterRepository;
import com.jobpilot.application.coverletter.usecase.CreateCoverLetterUseCase;
import com.jobpilot.application.coverletter.usecase.GenerateCoverLetterUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.domain.coverletter.CoverLetterId;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cover-letters")
public class CoverLetterController {

    private final GenerateCoverLetterUseCase generateCoverLetterUseCase;
    private final CreateCoverLetterUseCase createCoverLetterUseCase;
    private final CoverLetterRepository coverLetterRepository;

    public CoverLetterController(GenerateCoverLetterUseCase generateCoverLetterUseCase,
                                 CreateCoverLetterUseCase createCoverLetterUseCase,
                                 CoverLetterRepository coverLetterRepository) {
        this.generateCoverLetterUseCase = generateCoverLetterUseCase;
        this.createCoverLetterUseCase = createCoverLetterUseCase;
        this.coverLetterRepository = coverLetterRepository;
    }

    @RateLimited(capacity = 100)
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> generate(
            @Valid @RequestBody GenerateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new GenerateCoverLetterCommand(
            UUID.fromString(principal.userId()),
            request.companyName(),
            request.jobTitle(),
            request.tone(),
            request.recipientName()
        );
        var response = generateCoverLetterUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<CoverLetterResponse>> create(
            @Valid @RequestBody CreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new CreateCoverLetterCommand(
            UUID.fromString(principal.userId()),
            request.title(),
            request.companyName(),
            request.jobTitle(),
            request.content(),
            request.tone()
        );
        var response = createCoverLetterUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CoverLetterResponse>>> list(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var letters = coverLetterRepository.findByUserId(userId).stream()
            .map(CoverLetterResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(letters));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> getById(@PathVariable String id) {
        var coverLetterId = CoverLetterId.from(UUID.fromString(id));
        var coverLetter = coverLetterRepository.findById(coverLetterId)
            .orElseThrow(() -> new NotFoundException("CoverLetter", id));
        return ResponseEntity.ok(ApiResponse.ok(CoverLetterResponse.from(coverLetter)));
    }

    @RateLimited(capacity = 100)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var coverLetterId = CoverLetterId.from(UUID.fromString(id));
        var coverLetter = coverLetterRepository.findById(coverLetterId)
            .orElseThrow(() -> new NotFoundException("CoverLetter", id));
        coverLetter.softDelete();
        coverLetterRepository.save(coverLetter);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record GenerateRequest(
        @NotBlank @Size(max = 255) String companyName,
        @NotBlank @Size(max = 255) String jobTitle,
        String tone,
        @Size(max = 255) String recipientName
    ) {}

    public record CreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String companyName,
        @NotBlank @Size(max = 255) String jobTitle,
        @NotBlank String content,
        String tone
    ) {}
}
