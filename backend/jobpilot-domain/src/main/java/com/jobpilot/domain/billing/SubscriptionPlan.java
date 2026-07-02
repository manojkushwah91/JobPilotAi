package com.jobpilot.domain.billing;

import java.math.BigDecimal;
import java.util.List;

public enum SubscriptionPlan {
    FREE(BigDecimal.ZERO, List.of("Basic job search", "Limited applications")),
    PREMIUM(new BigDecimal("29.99"), List.of("All Free features", "Unlimited applications", "AI resume builder", "Priority support")),
    PRO(new BigDecimal("79.99"), List.of("All Premium features", "Career coaching", "Interview prep", "Analytics dashboard"));

    private final BigDecimal monthlyPrice;
    private final List<String> features;

    SubscriptionPlan(BigDecimal monthlyPrice, List<String> features) {
        this.monthlyPrice = monthlyPrice;
        this.features = features;
    }

    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public List<String> getFeatures() { return features; }
}
