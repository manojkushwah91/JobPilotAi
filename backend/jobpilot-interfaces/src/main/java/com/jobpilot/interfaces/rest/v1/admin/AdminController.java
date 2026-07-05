package com.jobpilot.interfaces.rest.v1.admin;

import com.jobpilot.application.admin.dto.AuditLogResponse;
import com.jobpilot.application.admin.dto.FeatureFlagRequest;
import com.jobpilot.application.admin.dto.FeatureFlagResponse;
import com.jobpilot.application.admin.dto.UpdateFeatureFlagCommand;
import com.jobpilot.application.admin.service.AuditLogService;
import com.jobpilot.application.admin.service.GetAllFeatureFlagsService;
import com.jobpilot.application.admin.usecase.ToggleFeatureUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.persistence.application.ApplicationJpaRepository;
import com.jobpilot.infrastructure.persistence.billing.SubscriptionJpaRepository;
import com.jobpilot.infrastructure.persistence.identity.UserEntity;
import com.jobpilot.infrastructure.persistence.identity.UserJpaRepository;
import com.jobpilot.infrastructure.persistence.job.JobListingJpaRepository;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final GetAllFeatureFlagsService getAllFeatureFlagsService;
    private final ToggleFeatureUseCase toggleFeatureUseCase;
    private final AuditLogService auditLogService;
    private final UserJpaRepository userJpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final JobListingJpaRepository jobListingJpaRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    public AdminController(GetAllFeatureFlagsService getAllFeatureFlagsService,
                            ToggleFeatureUseCase toggleFeatureUseCase,
                            AuditLogService auditLogService,
                            UserJpaRepository userJpaRepository,
                            ApplicationJpaRepository applicationJpaRepository,
                            JobListingJpaRepository jobListingJpaRepository,
                            SubscriptionJpaRepository subscriptionJpaRepository) {
        this.getAllFeatureFlagsService = getAllFeatureFlagsService;
        this.toggleFeatureUseCase = toggleFeatureUseCase;
        this.auditLogService = auditLogService;
        this.userJpaRepository = userJpaRepository;
        this.applicationJpaRepository = applicationJpaRepository;
        this.jobListingJpaRepository = jobListingJpaRepository;
        this.subscriptionJpaRepository = subscriptionJpaRepository;
    }

    @RateLimited(capacity = 100)
    @GetMapping("/feature-flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getFeatureFlags() {
        var response = getAllFeatureFlagsService.execute(null);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/feature-flags/{key}")
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

    @RateLimited(capacity = 100)
    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLog(Pageable pageable) {
        var response = auditLogService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 50)
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listUsers() {
        var users = userJpaRepository.findAll().stream().<Map<String, Object>>map(u -> Map.of(
            "id", u.getId().toString(),
            "email", u.getEmail(),
            "name", u.getName() != null ? u.getName() : "",
            "role", u.getRole().name(),
            "emailVerified", u.isEmailVerified(),
            "lastLoginAt", u.getLastLoginAt() != null ? u.getLastLoginAt().toString() : null
        )).toList();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @RateLimited(capacity = 50)
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUser(
            @PathVariable String id) {
        var user = userJpaRepository.findById(UUID.fromString(id));
        if (user.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of()));
        }
        var u = user.get();
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "id", u.getId().toString(),
            "email", u.getEmail(),
            "name", u.getName() != null ? u.getName() : "",
            "role", u.getRole().name(),
            "emailVerified", u.isEmailVerified(),
            "lastLoginAt", u.getLastLoginAt() != null ? u.getLastLoginAt().toString() : null,
            "deletedAt", u.getDeletedAt() != null ? u.getDeletedAt().toString() : null
        )));
    }

    @RateLimited(capacity = 50)
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        var user = userJpaRepository.findById(UUID.fromString(id));
        if (user.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of()));
        }
        var u = user.get();
        if (body.containsKey("name")) u.setName((String) body.get("name"));
        if (body.containsKey("role")) u.setRole(com.jobpilot.domain.identity.Role.valueOf((String) body.get("role")));
        userJpaRepository.save(u);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "id", u.getId().toString(),
            "email", u.getEmail(),
            "name", u.getName() != null ? u.getName() : "",
            "role", u.getRole().name()
        )));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable String id) {
        var user = userJpaRepository.findById(UUID.fromString(id));
        user.ifPresent(u -> {
            u.setDeletedAt(java.time.Instant.now());
            userJpaRepository.save(u);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/users/{id}/unsuspend")
    public ResponseEntity<ApiResponse<Void>> unsuspendUser(@PathVariable String id) {
        var user = userJpaRepository.findById(UUID.fromString(id));
        user.ifPresent(u -> {
            u.setDeletedAt(null);
            userJpaRepository.save(u);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 50)
    @GetMapping("/metrics/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics() {
        long totalUsers = userJpaRepository.count();
        long activeUsers = userJpaRepository.findAll().stream()
            .filter(u -> u.getDeletedAt() == null).count();
        long totalApplications = applicationJpaRepository.count();
        long totalJobs = jobListingJpaRepository.count();
        long premiumUsers = subscriptionJpaRepository.count();
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "totalUsers", totalUsers, "activeUsers", activeUsers,
            "totalApplications", totalApplications, "totalJobs", totalJobs,
            "premiumUsers", premiumUsers, "revenue", Map.of(
                "total", 0, "monthly", 0, "currency", "USD"
            )
        )));
    }
}
