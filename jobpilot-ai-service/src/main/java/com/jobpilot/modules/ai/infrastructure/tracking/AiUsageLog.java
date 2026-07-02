package com.jobpilot.modules.ai.infrastructure.tracking;

import com.jobpilot.modules.ai.domain.model.PromptUseCase;
import com.jobpilot.modules.ai.domain.model.TokenUsage;

public record AiUsageLog(
    PromptUseCase useCase,
    String provider,
    String model,
    TokenUsage usage,
    long latencyMs,
    boolean cacheHit
) {
    public long costMicroUsd() {
        return usage.estimatedCostMicroUsd(model);
    }
}
