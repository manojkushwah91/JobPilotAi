package com.jobpilot.interfaces.rest.v1.settings;

import com.jobpilot.application.billing.dto.CancelSubscriptionCommand;
import com.jobpilot.application.billing.dto.StartSubscriptionCommand;
import com.jobpilot.application.billing.ports.InvoiceRepository;
import com.jobpilot.application.billing.ports.SubscriptionRepository;
import com.jobpilot.application.billing.usecase.CancelSubscriptionUseCase;
import com.jobpilot.application.billing.usecase.StartSubscriptionUseCase;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.user.ports.UserSettingsRepository;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.domain.billing.SubscriptionPlan;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final StartSubscriptionUseCase startSubscriptionUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public SettingsController(StartSubscriptionUseCase startSubscriptionUseCase,
                               CancelSubscriptionUseCase cancelSubscriptionUseCase,
                               SubscriptionRepository subscriptionRepository,
                               InvoiceRepository invoiceRepository,
                               UserRepository userRepository,
                               UserSettingsRepository userSettingsRepository) {
        this.startSubscriptionUseCase = startSubscriptionUseCase;
        this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSettings(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var user = userRepository.findById(UserId.from(userId));
        var settings = userSettingsRepository.findByUserId(userId);
        Map<String, Object> profile = user.map(u -> Map.<String, Object>of(
            "name", u.name() != null ? u.name() : "",
            "email", u.email().value(),
            "locale", "en"
        )).orElse(Map.of());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "profile", profile,
            "jobPreferences", settings.map(s -> s.jobPreferences()).orElse(Map.of()),
            "privacy", settings.map(s -> s.privacySettings()).orElse(Map.of()),
            "ai", settings.map(s -> s.aiPreferences()).orElse(Map.of()),
            "billing", Map.of()
        )));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfileSettings(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var user = userRepository.findById(UserId.from(userId));
        return ResponseEntity.ok(ApiResponse.ok(user.map(u -> Map.<String, Object>of(
            "name", u.name() != null ? u.name() : "",
            "email", u.email().value(),
            "avatarUrl", "",
            "locale", "en"
        )).orElse(Map.of())));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/job-preferences")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJobPreferences(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var settings = userSettingsRepository.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(settings
            .map(s -> s.jobPreferences())
            .orElse(Map.of(
                "desiredRoles", List.of(),
                "locations", List.of(),
                "remotePreference", "HYBRID",
                "salaryMin", 0,
                "salaryMax", 0,
                "employmentTypes", List.of()
            ))));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/privacy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPrivacySettings(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var settings = userSettingsRepository.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(settings
            .map(s -> s.privacySettings())
            .orElse(Map.of(
                "profileVisibility", "PUBLIC",
                "showSalary", false,
                "shareDataWithEmployers", true
            ))));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/ai")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiSettings(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var settings = userSettingsRepository.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(settings
            .map(s -> s.aiPreferences())
            .orElse(Map.of(
                "preferredProvider", "OPENAI",
                "model", "gpt-4",
                "autoGenerateCoverLetters", false,
                "autoTailorResumes", true
            ))));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/api-keys")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApiKeys(
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/api-keys/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiKey(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "id", id, "name", "", "key", "sk-...", "createdAt", ""
        )));
    }

    // --- Billing endpoints ---

    @RateLimited(capacity = 100)
    @GetMapping("/billing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBilling(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var sub = subscriptionRepository.findByUserId(UUID.fromString(principal.userId()));
        var result = sub.<Map<String, Object>>map(s -> Map.of(
            "tier", s.plan().name(),
            "status", s.status().name(),
            "currentPeriodStart", s.startedAt().toString(),
            "currentPeriodEnd", s.expiresAt().toString()
        )).orElse(Map.of(
            "tier", "FREE",
            "status", "ACTIVE",
            "currentPeriodStart", java.time.Instant.now().toString(),
            "currentPeriodEnd", java.time.Instant.now().plusSeconds(86400 * 30).toString()
        ));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/billing/invoices")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInvoices(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var invoices = invoiceRepository.findByUserId(UUID.fromString(principal.userId()));
        var result = invoices.stream().<Map<String, Object>>map(inv -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", inv.invoiceId().value().toString());
            m.put("amount", inv.amount());
            m.put("currency", inv.currency());
            m.put("status", inv.status());
            m.put("date", inv.createdAt().toString());
            m.put("pdfUrl", inv.pdfUrl() != null ? inv.pdfUrl() : "");
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 50)
    @DeleteMapping("/billing/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSubscriptionDelete(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        subscriptionRepository.findByUserId(userId).ifPresent(sub ->
            cancelSubscriptionUseCase.execute(new CancelSubscriptionCommand(sub.subscriptionId().value().toString())));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/billing/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        subscriptionRepository.findByUserId(userId).ifPresent(sub ->
            cancelSubscriptionUseCase.execute(new CancelSubscriptionCommand(sub.subscriptionId().value().toString())));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 50)
    @PutMapping("/billing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateBilling(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var tier = body.getOrDefault("tier", "FREE");
        var command = new StartSubscriptionCommand(userId, SubscriptionPlan.valueOf(tier));
        var response = startSubscriptionUseCase.execute(command);
        Map<String, Object> result = Map.of(
            "tier", response.plan().name(),
            "status", response.status().name(),
            "currentPeriodStart", response.startedAt().toString(),
            "currentPeriodEnd", response.expiresAt().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
