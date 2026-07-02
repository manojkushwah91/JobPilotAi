package com.jobpilot.infrastructure.persistence.billing;

import com.jobpilot.domain.billing.Subscription;
import com.jobpilot.domain.billing.SubscriptionId;
import com.jobpilot.domain.billing.SubscriptionPlan;
import com.jobpilot.domain.billing.SubscriptionStatus;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "plan", nullable = false) private String plan;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "cancelled_at") private Instant cancelledAt;

    protected SubscriptionEntity() {}

    public static SubscriptionEntity fromDomain(Subscription s) {
        var e = new SubscriptionEntity();
        e.id = s.subscriptionId().value();
        e.userId = s.userId();
        e.plan = s.plan().name();
        e.status = s.status().name();
        e.startedAt = s.startedAt();
        e.expiresAt = s.expiresAt();
        e.cancelledAt = s.cancelledAt();
        return e;
    }

    public Subscription toDomain() {
        return Subscription.reconstitute(
            SubscriptionId.from(id), userId, SubscriptionPlan.valueOf(plan),
            SubscriptionStatus.valueOf(status), startedAt, expiresAt, cancelledAt,
            createdAt, updatedAt
        );
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getPlan() { return plan; }
    public String getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCancelledAt() { return cancelledAt; }
}
