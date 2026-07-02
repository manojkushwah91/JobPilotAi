package com.jobpilot.modules.ai.infrastructure.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

@Component
public class UsageTracker {

    private static final Logger log = LoggerFactory.getLogger(UsageTracker.class);

    private final AtomicLong totalTokens = new AtomicLong();
    private final AtomicLong totalCostMicroUsd = new AtomicLong();
    private final List<AiUsageLog> recentLogs = new CopyOnWriteArrayList<>();

    public void track(AiUsageLog logEntry) {
        totalTokens.addAndGet(logEntry.usage().totalTokens());
        totalCostMicroUsd.addAndGet(logEntry.costMicroUsd());
        recentLogs.add(logEntry);
        if (recentLogs.size() > 10_000) {
            recentLogs.remove(0);
        }
        log.info("AI usage: useCase={} provider={} model={} tokens={} cost={}µ$ latency={}ms",
            logEntry.useCase(), logEntry.provider(), logEntry.model(),
            logEntry.usage().totalTokens(), logEntry.costMicroUsd(), logEntry.latencyMs());
    }

    public long getTotalTokens() { return totalTokens.get(); }
    public long getTotalCostMicroUsd() { return totalCostMicroUsd.get(); }
    public List<AiUsageLog> getRecentLogs() { return List.copyOf(recentLogs); }
}
