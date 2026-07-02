package com.jobpilot.application.billing.dto;

import com.jobpilot.common.exception.ValidationException;

public record CancelSubscriptionCommand(String subscriptionId) {
    public CancelSubscriptionCommand {
        if (subscriptionId == null || subscriptionId.isBlank())
            throw new ValidationException("subscriptionId", "Subscription ID must not be blank");
    }
}
