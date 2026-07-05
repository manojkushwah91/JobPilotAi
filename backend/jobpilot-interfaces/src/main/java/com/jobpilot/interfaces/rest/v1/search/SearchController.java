package com.jobpilot.interfaces.rest.v1.search;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.search.dto.VectorSearchRequest;
import com.jobpilot.application.search.ports.VectorSearchPort;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.persistence.job.JobListingJpaRepository;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final VectorSearchPort vectorSearchPort;
    private final JobListingJpaRepository jobListingJpaRepository;

    public SearchController(VectorSearchPort vectorSearchPort, JobListingJpaRepository jobListingJpaRepository) {
        this.vectorSearchPort = vectorSearchPort;
        this.jobListingJpaRepository = jobListingJpaRepository;
    }

    @RateLimited(capacity = 100)
    @PostMapping("/jobs/similar")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> searchSimilar(@RequestBody VectorSearchRequest request, Pageable pageable) {
        if (request.skills() != null && !request.skills().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(
                vectorSearchPort.searchBySkills(request.skills(), pageable).map(JobResponse::from)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
            vectorSearchPort.searchSimilar(request.query(), pageable).map(JobResponse::from)));
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var results = vectorSearchPort.searchSimilar(q, pageable);
        var jobs = results.getContent().stream().<Map<String, Object>>map(j -> Map.of(
            "id", j.jobId().value().toString(),
            "title", j.title(),
            "companyName", j.companyName(),
            "location", j.location() != null ? j.location() : Map.of(),
            "salary", j.salary() != null ? j.salary() : Map.of(),
            "employmentType", j.employmentType() != null ? j.employmentType().name() : "",
            "postedAt", j.postedAt() != null ? j.postedAt().toString() : ""
        )).toList();
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "query", q,
            "results", jobs,
            "total", results.getTotalElements(),
            "page", page,
            "size", size
        )));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> suggestions(@RequestParam String q) {
        var results = jobListingJpaRepository.search(q, PageRequest.of(0, 10));
        var suggestions = results.getContent().stream()
            .<String>flatMap(j -> java.util.stream.Stream.of(j.getTitle(), j.getCompanyName()))
            .filter(s -> s != null && !s.isBlank())
            .distinct()
            .limit(10)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(suggestions));
    }
}
