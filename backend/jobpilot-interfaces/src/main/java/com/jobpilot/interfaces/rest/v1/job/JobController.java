package com.jobpilot.interfaces.rest.v1.job;

import com.jobpilot.application.job.dto.*;
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

    public JobController(CreateJobUseCase createJobUseCase, UpdateJobUseCase updateJobUseCase,
                          DeleteJobUseCase deleteJobUseCase, GetJobUseCase getJobUseCase,
                          SearchJobsUseCase searchJobsUseCase) {
        this.createJobUseCase = createJobUseCase;
        this.updateJobUseCase = updateJobUseCase;
        this.deleteJobUseCase = deleteJobUseCase;
        this.getJobUseCase = getJobUseCase;
        this.searchJobsUseCase = searchJobsUseCase;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var command = new SearchJobsCommand(query, null, null, null, null, null, page, size);
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
