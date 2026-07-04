package com.jobpilot.interfaces.rest.v1.job;

import com.jobpilot.application.job.dto.JobResponse;
import com.jobpilot.application.job.dto.SaveJobCommand;
import com.jobpilot.application.job.usecase.GetSavedJobsUseCase;
import com.jobpilot.application.job.usecase.SaveJobUseCase;
import com.jobpilot.application.job.usecase.UnsaveJobUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class SavedJobController {

    private final SaveJobUseCase saveJobUseCase;
    private final UnsaveJobUseCase unsaveJobUseCase;
    private final GetSavedJobsUseCase getSavedJobsUseCase;

    public SavedJobController(SaveJobUseCase saveJobUseCase, UnsaveJobUseCase unsaveJobUseCase,
                               GetSavedJobsUseCase getSavedJobsUseCase) {
        this.saveJobUseCase = saveJobUseCase;
        this.unsaveJobUseCase = unsaveJobUseCase;
        this.getSavedJobsUseCase = getSavedJobsUseCase;
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<JobResponse>> save(@AuthenticationPrincipal JwtPrincipal principal,
                                                          @PathVariable String id,
                                                          @RequestBody(required = false) SaveRequest body) {
        var response = saveJobUseCase.execute(new SaveJobCommand(principal.userId(), id, body != null ? body.notes() : null));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity<ApiResponse<Void>> unsave(@AuthenticationPrincipal JwtPrincipal principal,
                                                     @PathVariable String id) {
        unsaveJobUseCase.execute(new SaveJobCommand(principal.userId(), id, null));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getSaved(@AuthenticationPrincipal JwtPrincipal principal) {
        var response = getSavedJobsUseCase.execute(principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    public record SaveRequest(String notes) {}
}
