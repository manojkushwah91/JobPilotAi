package com.jobpilot.interfaces.rest.v1.admin;

import com.jobpilot.application.admin.dto.AuditLogResponse;
import com.jobpilot.application.admin.dto.FeatureFlagRequest;
import com.jobpilot.application.admin.dto.FeatureFlagResponse;
import com.jobpilot.application.admin.dto.UpdateFeatureFlagCommand;
import com.jobpilot.application.admin.service.AuditLogService;
import com.jobpilot.application.admin.service.GetAllFeatureFlagsService;
import com.jobpilot.application.admin.usecase.ToggleFeatureUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final GetAllFeatureFlagsService getAllFeatureFlagsService;
    private final ToggleFeatureUseCase toggleFeatureUseCase;
    private final AuditLogService auditLogService;

    public AdminController(GetAllFeatureFlagsService getAllFeatureFlagsService,
                            ToggleFeatureUseCase toggleFeatureUseCase,
                            AuditLogService auditLogService) {
        this.getAllFeatureFlagsService = getAllFeatureFlagsService;
        this.toggleFeatureUseCase = toggleFeatureUseCase;
        this.auditLogService = auditLogService;
    }

    @RateLimited(capacity = 100)
    @GetMapping("/feature-flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getFeatureFlags() {
        var response = getAllFeatureFlagsService.execute(null);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PutMapping("/feature-flags/{key}")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> toggleFeature(
            @PathVariable String key, @RequestBody FeatureFlagRequest request) {
        var response = toggleFeatureUseCase.execute(new UpdateFeatureFlagCommand(key, request.enabled()));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(Pageable pageable) {
        var response = auditLogService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
