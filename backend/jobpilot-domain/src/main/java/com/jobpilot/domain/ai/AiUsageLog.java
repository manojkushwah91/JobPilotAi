package com.jobpilot.domain.ai;

import com.jobpilot.domain.shared.BaseEntity;

import java.time.Instant;
import java.util.UUID;

public class AiUsageLog extends BaseEntity {

    private final AiUsageLogId aiUsageLogId;
    private UUID userId;
    private String useCase;
    private String provider;
    private String model;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private long costMicroUsd;
    private int latencyMs;
    private boolean cacheHit;
    private final Instant createdAt;

    public AiUsageLog(AiUsageLogId aiUsageLogId, UUID userId, String useCase, String provider, String model,
                       int promptTokens, int completionTokens, long costMicroUsd, int latencyMs, boolean cacheHit) {
        super(aiUsageLogId.value());
        this.aiUsageLogId = aiUsageLogId;
        this.userId = userId;
        this.useCase = useCase;
        this.provider = provider;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
        this.costMicroUsd = costMicroUsd;
        this.latencyMs = latencyMs;
        this.cacheHit = cacheHit;
        this.createdAt = Instant.now();
    }

    public AiUsageLogId aiUsageLogId() { return aiUsageLogId; }
    public UUID userId() { return userId; }
    public String useCase() { return useCase; }
    public String provider() { return provider; }
    public String model() { return model; }
    public int promptTokens() { return promptTokens; }
    public int completionTokens() { return completionTokens; }
    public int totalTokens() { return totalTokens; }
    public long costMicroUsd() { return costMicroUsd; }
    public int latencyMs() { return latencyMs; }
    public boolean cacheHit() { return cacheHit; }
    public Instant createdAt() { return createdAt; }
}
