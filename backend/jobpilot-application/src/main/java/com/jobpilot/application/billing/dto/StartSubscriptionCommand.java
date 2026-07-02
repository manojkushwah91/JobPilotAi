package com.jobpilot.application.billing.dto;

import com.jobpilot.common.exception.ValidationException;
import com.jobpilot.domain.billing.SubscriptionPlan;
import java.util.UUID;

public record StartSubscriptionCommand(UUID userId, SubscriptionPlan plan) {
    public StartSubscriptionCommand {
        if (userId == null) throw new ValidationException("userId", "User ID must not be null");
        if (plan == null) throw new ValidationException("plan", "Plan must not be null");
    }
}
