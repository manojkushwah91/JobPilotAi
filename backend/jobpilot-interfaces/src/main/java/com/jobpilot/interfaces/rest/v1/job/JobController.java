package com.jobpilot.interfaces.rest.v1.job;

import com.jobpilot.application.job.dto.*;
import com.jobpilot.application.job.service.JobMatchingService;
import com.jobpilot.application.job.usecase.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final CreateJobUseCase createJobUseCase;
    private final UpdateJobUseCase updateJobUseCase;
    private final DeleteJobUseCase deleteJobUseCase;
    private final GetJobUseCase getJobUseCase;
    private final SearchJobsUseCase searchJobsUseCase;
    private final JobMatchingService jobMatchingService;

    public JobController(CreateJobUseCase createJobUseCase, UpdateJobUseCase updateJobUseCase,
                          DeleteJobUseCase deleteJobUseCase, GetJobUseCase getJobUseCase,
                          SearchJobsUseCase searchJobsUseCase,
                          JobMatchingService jobMatchingService) {
        this.createJobUseCase = createJobUseCase;
        this.updateJobUseCase = updateJobUseCase;
        this.deleteJobUseCase = deleteJobUseCase;
        this.getJobUseCase = getJobUseCase;
        this.searchJobsUseCase = searchJobsUseCase;
        this.jobMatchingService = jobMatchingService;
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> create(@Valid @RequestBody CreateJobRequest request) {
        var command = new CreateJobCommand(request.title(), request.companyName(), request.description(),
            request.location(), request.salary(), request.employmentType(), request.experienceLevel(),
            request.industry(), request.skills(), request.applicationUrl());
        var response = createJobUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getById(@PathVariable String id) {
        var response = getJobUseCase.execute(new GetJobCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String postedWithin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        var searchQuery = query != null ? query : keyword;
        var command = new SearchJobsCommand(searchQuery, null, employmentType, experienceLevel,
            industry, location, salaryMin, salaryMax, postedWithin, page, size);
        var response = searchJobsUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> update(@PathVariable String id,
            @Valid @RequestBody CreateJobRequest request) {
        var command = new UpdateJobCommand(id, request.title(), request.companyName(), request.description(),
            request.location(), request.salary(), request.employmentType(), request.experienceLevel(),
            request.industry(), request.skills(), request.applicationUrl());
        var response = updateJobUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        deleteJobUseCase.execute(new DeleteJobCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 50)
    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMatches(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var matches = jobMatchingService.getMatches(principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(matches));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<JobResponse>>> recommendations(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "10") int limit) {
        var command = new SearchJobsCommand(null, null, null, null, null, null, null, null, null, 0, limit);
        var response = searchJobsUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response.getContent()));
    }

    @RateLimited(capacity = 50)
    @GetMapping("/semantic-search")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> semanticSearch(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        var command = new SearchJobsCommand(query, null, null, null, null, null, null, null, null, page, size);
        var response = searchJobsUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/semantic-search")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> semanticSearchPost(
            @RequestBody Map<String, Object> body,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        var query = (String) body.getOrDefault("query", "");
        var command = new SearchJobsCommand(query, null, null, null, null, null, null, null, null, page, size);
        var response = searchJobsUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/facets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> facets() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "employmentTypes", List.of("FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "TEMPORARY"),
            "experienceLevels", List.of("ENTRY", "JUNIOR", "MID", "SENIOR", "LEAD", "EXECUTIVE"),
            "industries", List.of(),
            "locations", List.of(),
            "salaryRanges", List.of(Map.of("min", 0, "max", 50000), Map.of("min", 50000, "max", 100000),
                Map.of("min", 100000, "max", 150000), Map.of("min", 150000, "max", 200000))
        )));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/aggregate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> aggregate() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "totalJobs", 0, "bySource", Map.of(), "byIndustry", Map.of(),
            "byEmploymentType", Map.of(), "avgSalary", 0
        )));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/matches/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMatchDetail(
            @AuthenticationPrincipal JwtPrincipal principal, @PathVariable String id) {
        var analysis = jobMatchingService.analyzeMatch(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(analysis));
    }

    public record CreateJobRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String companyName,
        @NotBlank String description,
        Map<String, Object> location,
        Map<String, Object> salary,
        String employmentType,
        String experienceLevel,
        String industry,
        List<String> skills,
        String applicationUrl
    ) {}
}
