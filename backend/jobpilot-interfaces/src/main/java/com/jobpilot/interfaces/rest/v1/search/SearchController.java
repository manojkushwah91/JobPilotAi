package com.jobpilot.interfaces.rest.v1.search;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.search.dto.VectorSearchRequest;
import com.jobpilot.application.search.ports.VectorSearchPort;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final VectorSearchPort vectorSearchPort;

    public SearchController(VectorSearchPort vectorSearchPort) { this.vectorSearchPort = vectorSearchPort; }

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
}
