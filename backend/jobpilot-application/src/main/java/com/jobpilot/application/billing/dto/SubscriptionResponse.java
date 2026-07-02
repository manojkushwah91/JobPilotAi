package com.jobpilot.application.billing.dto;

import com.jobpilot.domain.billing.Subscription;
import com.jobpilot.domain.billing.SubscriptionPlan;
import com.jobpilot.domain.billing.SubscriptionStatus;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
    String subscriptionId, UUID userId, SubscriptionPlan plan, SubscriptionStatus status,
    Instant startedAt, Instant expiresAt, Instant cancelledAt,
    Instant createdAt, Instant updatedAt
) {
    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
            s.subscriptionId().value().toString(), s.userId(), s.plan(), s.status(),
            s.startedAt(), s.expiresAt(), s.cancelledAt(),
            s.createdAt(), s.updatedAt()
        );
    }
}
