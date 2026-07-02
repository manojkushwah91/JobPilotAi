package com.jobpilot.infrastructure.persistence.ai;

import com.jobpilot.domain.ai.AiUsageLog;
import com.jobpilot.domain.ai.AiUsageLogId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ai_usage_logs")
public class AiUsageLogEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "use_case", nullable = false) private String useCase;
    @Column(name = "provider", nullable = false) private String provider;
    @Column(name = "model", nullable = false) private String model;
    @Column(name = "prompt_tokens", nullable = false) private int promptTokens;
    @Column(name = "completion_tokens", nullable = false) private int completionTokens;
    @Column(name = "total_tokens", nullable = false) private int totalTokens;
    @Column(name = "cost_micro_usd", nullable = false) private long costMicroUsd;
    @Column(name = "latency_ms", nullable = false) private int latencyMs;
    @Column(name = "cache_hit", nullable = false) private boolean cacheHit;

    protected AiUsageLogEntity() {}

    public static AiUsageLogEntity fromDomain(AiUsageLog log) {
        var e = new AiUsageLogEntity();
        e.id = log.aiUsageLogId().value();
        e.userId = log.userId();
        e.useCase = log.useCase();
        e.provider = log.provider();
        e.model = log.model();
        e.promptTokens = log.promptTokens();
        e.completionTokens = log.completionTokens();
        e.totalTokens = log.totalTokens();
        e.costMicroUsd = log.costMicroUsd();
        e.latencyMs = log.latencyMs();
        e.cacheHit = log.cacheHit();
        return e;
    }

    public AiUsageLog toDomain() {
        return new AiUsageLog(AiUsageLogId.from(id), userId, useCase, provider, model,
            promptTokens, completionTokens, costMicroUsd, latencyMs, cacheHit);
    }
}
