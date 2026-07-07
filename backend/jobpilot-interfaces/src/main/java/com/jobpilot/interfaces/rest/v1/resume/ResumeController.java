package com.jobpilot.interfaces.rest.v1.resume;

import com.jobpilot.application.ai.dto.AiResumeScoreRequest;
import com.jobpilot.application.ai.ports.AiResumeScoringPort;
import com.jobpilot.application.agent.service.CandidateProfileService;
import com.jobpilot.application.resume.dto.*;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.application.resume.service.ResumeParserService;
import com.jobpilot.application.resume.usecase.*;
import com.jobpilot.application.storage.service.FileUploadService;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final AiResumeScoringPort aiResumeScoringPort;
    private final FileUploadService fileUploadService;
    private final ResumeRepository resumeRepository;
    private final ResumeParserService resumeParserService;
    private final CandidateProfileService candidateProfileService;

    public ResumeController(CreateResumeUseCase createResumeUseCase,
                            UpdateResumeUseCase updateResumeUseCase,
                            DeleteResumeUseCase deleteResumeUseCase,
                            GetResumeUseCase getResumeUseCase,
                            ListResumesUseCase listResumesUseCase,
                            AiResumeScoringPort aiResumeScoringPort,
                            FileUploadService fileUploadService,
                            ResumeRepository resumeRepository,
                            ResumeParserService resumeParserService,
                            CandidateProfileService candidateProfileService) {
        this.createResumeUseCase = createResumeUseCase;
        this.updateResumeUseCase = updateResumeUseCase;
        this.deleteResumeUseCase = deleteResumeUseCase;
        this.getResumeUseCase = getResumeUseCase;
        this.listResumesUseCase = listResumesUseCase;
        this.aiResumeScoringPort = aiResumeScoringPort;
        this.fileUploadService = fileUploadService;
        this.resumeRepository = resumeRepository;
        this.resumeParserService = resumeParserService;
        this.candidateProfileService = candidateProfileService;
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

    @RateLimited(capacity = 50)
    @PostMapping("/{id}/score")
    public ResponseEntity<ApiResponse<Map<String, Object>>> score(
            @PathVariable String id,
            @RequestBody(required = false) ScoreRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var jobDesc = request != null ? request.jobDescription() : null;
        var aiRequest = new AiResumeScoreRequest(id, jobDesc);
        var response = aiResumeScoringPort.scoreResume(aiRequest);
        var result = Map.<String, Object>of(
            "overallScore", response.atsScore(),
            "keywordMatches", response.scoreBreakdown() != null ? response.scoreBreakdown() : Map.of(),
            "missingKeywords", response.missingKeywords() != null ? response.missingKeywords() : List.of(),
            "suggestions", buildSuggestions(response)
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/{id}/tailor")
    public ResponseEntity<ApiResponse<Map<String, Object>>> tailor(
            @PathVariable String id,
            @RequestBody TailorRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var resumeId = com.jobpilot.domain.resume.ResumeId.from(java.util.UUID.fromString(id));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + id));
        var sectionsText = new StringBuilder();
        for (var section : resume.sections()) {
            sectionsText.append(section.type()).append(": ")
                .append(section.content()).append("\n");
        }
        var result = Map.<String, Object>of(
            "resumeId", id,
            "originalSections", resume.sections().stream().map(s -> Map.<String, Object>of(
                "type", s.type().name(),
                "content", s.content() != null ? s.content().toString() : ""
            )).toList(),
            "tailoredSections", List.of(),
            "suggestedChanges", List.of(
                Map.of("section", "SUMMARY", "suggestion", "Highlight relevant experience for " + request.jobTitle()),
                Map.of("section", "SKILLS", "suggestion", "Emphasize skills matching the target role")
            )
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 50)
    @GetMapping("/{id}/export")
    public ResponseEntity<org.springframework.core.io.Resource> export(
            @PathVariable String id,
            @RequestParam(defaultValue = "pdf") String format) {
        var resumeId = com.jobpilot.domain.resume.ResumeId.from(java.util.UUID.fromString(id));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + id));
        var text = new StringBuilder();
        text.append("# ").append(resume.title()).append("\n\n");
        for (var section : resume.sections()) {
            text.append("## ").append(section.type().name()).append("\n");
            text.append(section.content()).append("\n\n");
        }
        var bytes = text.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var resource = new org.springframework.core.io.ByteArrayResource(bytes);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", resume.title().replace(" ", "_") + ".txt");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @RateLimited(capacity = 20)
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new com.jobpilot.application.storage.dto.UploadFileCommand(file, "resumes");
        var uploadResponse = fileUploadService.execute(command);
        var fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        var title = fileName.replaceAll("\\.[^.]*$", "");
        var createCommand = new CreateResumeCommand(principal.userId(), title, List.of());
        var resumeResponse = createResumeUseCase.execute(createCommand);
        var result = Map.<String, Object>of(
            "resumeId", resumeResponse.id(),
            "title", title,
            "filePath", uploadResponse.url(),
            "fileSize", uploadResponse.size(),
            "sectionCount", 0
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 10)
    @PostMapping("/upload-and-parse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadAndParse(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtPrincipal principal) throws Exception {
        var command = new com.jobpilot.application.storage.dto.UploadFileCommand(file, "resumes");
        var uploadResponse = fileUploadService.execute(command);
        var fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";

        var parsed = resumeParserService.parse(file.getBytes(), fileName);

        var createCommand = new CreateResumeCommand(principal.userId(), fileName.replaceAll("\\.[^.]*$", ""), List.of());
        var resumeResponse = createResumeUseCase.execute(createCommand);

        candidateProfileService.syncFromParsedResume(
            java.util.UUID.fromString(principal.userId()),
            parsed.fullText(),
            parsed.email(),
            parsed.phone(),
            parsed.linkedinUrl(),
            parsed.skills(),
            parsed.sections(),
            parsed.yearsExperience(),
            uploadResponse.url()
        );

        var result = Map.<String, Object>of(
            "resumeId", resumeResponse.id(),
            "title", fileName,
            "filePath", uploadResponse.url(),
            "fileSize", uploadResponse.size(),
            "parsed", Map.of(
                "email", parsed.email() != null ? parsed.email() : "",
                "phone", parsed.phone() != null ? parsed.phone() : "",
                "linkedinUrl", parsed.linkedinUrl() != null ? parsed.linkedinUrl() : "",
                "skills", parsed.skills(),
                "sections", parsed.sections(),
                "yearsExperience", parsed.yearsExperience()
            ),
            "profileSynced", true
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> parse(
            @RequestBody ParseRequest request) {
        var text = request.content() != null ? request.content() : "";
        var sections = new java.util.ArrayList<Map<String, Object>>();
        var lines = text.split("\n");
        var currentType = "SUMMARY";
        var currentContent = new StringBuilder();
        for (var line : lines) {
            var trimmed = line.trim();
            if (trimmed.toUpperCase().startsWith("SUMMARY") || trimmed.toUpperCase().startsWith("EXPERIENCE")
                || trimmed.toUpperCase().startsWith("EDUCATION") || trimmed.toUpperCase().startsWith("SKILLS")
                || trimmed.toUpperCase().startsWith("CERTIFICATIONS") || trimmed.toUpperCase().startsWith("PROJECTS")) {
                if (!currentContent.isEmpty()) {
                    sections.add(Map.of("type", currentType, "content", currentContent.toString().trim()));
                }
                currentType = trimmed.toUpperCase().replaceAll(":", "").trim();
                currentContent = new StringBuilder();
            } else {
                currentContent.append(trimmed).append(" ");
            }
        }
        if (!currentContent.isEmpty()) {
            sections.add(Map.of("type", currentType, "content", currentContent.toString().trim()));
        }
        var result = Map.<String, Object>of(
            "skills", extractBySection(sections, "SKILLS"),
            "experience", List.of(extractBySection(sections, "EXPERIENCE")),
            "education", List.of(extractBySection(sections, "EDUCATION")),
            "summary", extractBySection(sections, "SUMMARY"),
            "contact", "",
            "sections", sections
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/sections")
    public ResponseEntity<ApiResponse<List<SectionDto>>> getSections(@PathVariable String id) {
        var resumeId = com.jobpilot.domain.resume.ResumeId.from(java.util.UUID.fromString(id));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + id));
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var response = resume.sections().stream()
            .map(s -> new SectionDto(s.id().toString(), s.type().name(), s.title(),
                s.content(), s.sortOrder()))
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
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

    public record ScoreRequest(String jobDescription) {}

    public record TailorRequest(
        @NotBlank String jobTitle,
        String jobDescription,
        String companyName
    ) {}

    public record ParseRequest(String content) {}

    private List<SectionDto> toSectionDtos(List<SectionRequest> sections) {
        if (sections == null) return List.of();
        return sections.stream()
            .map(s -> new SectionDto(null, s.type(), s.title(), s.content(), s.sortOrder()))
            .toList();
    }

    private List<Map<String, Object>> buildSuggestions(
            com.jobpilot.application.ai.dto.AiResumeScoreResponse response) {
        var suggestions = new java.util.ArrayList<Map<String, Object>>();
        if (response.strengths() != null) {
            for (var s : response.strengths()) {
                suggestions.add(Map.of("category", "Strength", "severity", "MINOR", "message", s));
            }
        }
        if (response.improvements() != null) {
            for (var i : response.improvements()) {
                suggestions.add(Map.of("category", "Improvement", "severity", "MAJOR", "message", i));
            }
        }
        return suggestions;
    }

    private String extractBySection(List<Map<String, Object>> sections, String type) {
        return sections.stream()
            .filter(s -> type.equals(s.get("type")))
            .findFirst()
            .map(s -> (String) s.get("content"))
            .orElse("");
    }
}
