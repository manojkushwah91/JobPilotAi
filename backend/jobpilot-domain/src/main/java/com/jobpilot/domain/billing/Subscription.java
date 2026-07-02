package com.jobpilot.domain.billing;

import com.jobpilot.domain.shared.BaseAggregateRoot;
import java.time.Instant;
import java.util.UUID;

public class Subscription extends BaseAggregateRoot {

    private SubscriptionId subscriptionId;
    private UUID userId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private Instant startedAt;
    private Instant expiresAt;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;

    private Subscription() {
        super();
    }

    private Subscription(SubscriptionId subscriptionId, UUID userId, SubscriptionPlan plan) {
        super(subscriptionId.value());
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.plan = plan;
        this.status = SubscriptionStatus.ACTIVE;
        this.startedAt = Instant.now();
        this.expiresAt = startedAt.plusSeconds(30L * 24 * 60 * 60);
    }

    public static Subscription start(SubscriptionId subscriptionId, UUID userId, SubscriptionPlan plan) {
        var sub = new Subscription(subscriptionId, userId, plan);
        sub.registerEvent(new BillingEvent(subscriptionId.value(), userId, "subscription.started"));
        return sub;
    }

    public static Subscription reconstitute(SubscriptionId subscriptionId, UUID userId, SubscriptionPlan plan,
            SubscriptionStatus status, Instant startedAt, Instant expiresAt, Instant cancelledAt,
            Instant createdAt, Instant updatedAt) {
        var s = new Subscription();
        s.subscriptionId = subscriptionId;
        s.userId = userId;
        s.plan = plan;
        s.status = status;
        s.startedAt = startedAt;
        s.expiresAt = expiresAt;
        s.cancelledAt = cancelledAt;
        s.createdAt = createdAt;
        s.updatedAt = updatedAt;
        return s;
    }

    public void cancel() {
        if (status == SubscriptionStatus.CANCELLED) return;
        if (status == SubscriptionStatus.EXPIRED) throw new IllegalStateException("Cannot cancel expired subscription");
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        registerEvent(new BillingEvent(subscriptionId.value(), userId, "subscription.cancelled"));
    }

    public void renew() {
        if (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.EXPIRED) {
            this.status = SubscriptionStatus.ACTIVE;
            this.expiresAt = Instant.now().plusSeconds(30L * 24 * 60 * 60);
            this.cancelledAt = null;
            registerEvent(new BillingEvent(subscriptionId.value(), userId, "subscription.renewed"));
        }
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        registerEvent(new BillingEvent(subscriptionId.value(), userId, "subscription.expired"));
    }

    public void upgrade(SubscriptionPlan plan) {
        if (status != SubscriptionStatus.ACTIVE) throw new IllegalStateException("Cannot upgrade non-active subscription");
        this.plan = plan;
        registerEvent(new BillingEvent(subscriptionId.value(), userId, "subscription.upgraded"));
    }

    public SubscriptionId subscriptionId() { return subscriptionId; }
    public UUID userId() { return userId; }
    public SubscriptionPlan plan() { return plan; }
    public SubscriptionStatus status() { return status; }
    public Instant startedAt() { return startedAt; }
    public Instant expiresAt() { return expiresAt; }
    public Instant cancelledAt() { return cancelledAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
