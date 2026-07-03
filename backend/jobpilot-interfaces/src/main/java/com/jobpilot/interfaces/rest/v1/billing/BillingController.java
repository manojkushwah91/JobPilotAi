package com.jobpilot.interfaces.rest.v1.billing;

import com.jobpilot.application.billing.dto.CancelSubscriptionCommand;
import com.jobpilot.application.billing.dto.StartSubscriptionCommand;
import com.jobpilot.application.billing.dto.SubscriptionResponse;
import com.jobpilot.application.billing.ports.SubscriptionRepository;
import com.jobpilot.application.billing.usecase.CancelSubscriptionUseCase;
import com.jobpilot.application.billing.usecase.StartSubscriptionUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final StartSubscriptionUseCase startSubscriptionUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
    private final SubscriptionRepository subscriptionRepository;

    public BillingController(StartSubscriptionUseCase startSubscriptionUseCase,
                              CancelSubscriptionUseCase cancelSubscriptionUseCase,
                              SubscriptionRepository subscriptionRepository) {
        this.startSubscriptionUseCase = startSubscriptionUseCase;
        this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
        this.subscriptionRepository = subscriptionRepository;
    }

    @RateLimited(capacity = 100)
    @PostMapping("/subscriptions")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> create(
            @RequestBody StartSubscriptionRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var command = new StartSubscriptionCommand(userId, request.plan());
        var response = startSubscriptionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/subscriptions/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable String id) {
        cancelSubscriptionUseCase.execute(new CancelSubscriptionCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/subscriptions/user/{userId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getByUser(@PathVariable UUID userId) {
        var response = subscriptionRepository.findByUserId(userId)
            .map(SubscriptionResponse::from)
            .orElse(null);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    public record StartSubscriptionRequest(com.jobpilot.domain.billing.SubscriptionPlan plan) {}
}
