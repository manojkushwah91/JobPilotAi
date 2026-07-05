package com.jobpilot.infrastructure.automation.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BoardRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(BoardRateLimiter.class);

    private final Map<String, BoardLimits> boardLimits = new ConcurrentHashMap<>();
    private final Map<String, BoardStats> boardStats = new ConcurrentHashMap<>();

    @Value("${jobpilot.ratelimit.default-delay-ms:2000}")
    private int defaultDelayMs;

    @Value("${jobpilot.ratelimit.default-daily-cap:50}")
    private int defaultDailyCap;

    @Value("${jobpilot.ratelimit.default-hourly-cap:20}")
    private int defaultHourlyCap;

    public void initialize() {
        boardLimits.put("linkedin", new BoardLimits(3000, 40, 15));
        boardLimits.put("indeed", new BoardLimits(2500, 30, 12));
        boardLimits.put("greenhouse", new BoardLimits(1500, 60, 25));
        boardLimits.put("lever", new BoardLimits(1500, 60, 25));

        for (var board : boardLimits.keySet()) {
            boardStats.put(board, new BoardStats());
        }

        log.info("Rate limiter initialized with defaults: delay={}ms, dailyCap={}, hourlyCap={}",
            defaultDelayMs, defaultDailyCap, defaultHourlyCap);
    }

    public boolean canProceed(String boardName) {
        var limits = getLimits(boardName);
        var stats = boardStats.computeIfAbsent(boardName, k -> new BoardStats());

        if (stats.dailyCount.get() >= limits.dailyCap) {
            log.warn("Board {} hit daily cap ({})", boardName, limits.dailyCap);
            return false;
        }

        if (stats.hourlyCount.get() >= limits.hourlyCap) {
            log.warn("Board {} hit hourly cap ({})", boardName, limits.hourlyCap);
            return false;
        }

        var timeSinceLastRequest = System.currentTimeMillis() - stats.lastRequestTime;
        if (timeSinceLastRequest < limits.delayMs) {
            return false;
        }

        return true;
    }

    public void recordRequest(String boardName) {
        var stats = boardStats.computeIfAbsent(boardName, k -> new BoardStats());
        stats.dailyCount.incrementAndGet();
        stats.hourlyCount.incrementAndGet();
        stats.totalCount.incrementAndGet();
        stats.lastRequestTime = System.currentTimeMillis();
    }

    public long getWaitTimeMs(String boardName) {
        var limits = getLimits(boardName);
        var stats = boardStats.computeIfAbsent(boardName, k -> new BoardStats());

        var timeSinceLastRequest = System.currentTimeMillis() - stats.lastRequestTime;
        var delayRemaining = limits.delayMs - timeSinceLastRequest;

        return Math.max(0, delayRemaining);
    }

    public void resetHourly(String boardName) {
        var stats = boardStats.get(boardName);
        if (stats != null) {
            stats.hourlyCount.set(0);
            log.info("Reset hourly count for {}", boardName);
        }
    }

    public void resetDaily(String boardName) {
        var stats = boardStats.get(boardName);
        if (stats != null) {
            stats.dailyCount.set(0);
            log.info("Reset daily count for {}", boardName);
        }
    }

    public BoardStats getStats(String boardName) {
        return boardStats.getOrDefault(boardName, new BoardStats());
    }

    public BoardLimits getLimits(String boardName) {
        return boardLimits.getOrDefault(boardName,
            new BoardLimits(defaultDelayMs, defaultDailyCap, defaultHourlyCap));
    }

    public void setLimits(String boardName, int delayMs, int dailyCap, int hourlyCap) {
        boardLimits.put(boardName, new BoardLimits(delayMs, dailyCap, hourlyCap));
        log.info("Updated limits for {}: delay={}ms, dailyCap={}, hourlyCap={}",
            boardName, delayMs, dailyCap, hourlyCap);
    }

    public static class BoardLimits {
        public final int delayMs;
        public final int dailyCap;
        public final int hourlyCap;

        public BoardLimits(int delayMs, int dailyCap, int hourlyCap) {
            this.delayMs = delayMs;
            this.dailyCap = dailyCap;
            this.hourlyCap = hourlyCap;
        }
    }

    public static class BoardStats {
        public final AtomicInteger dailyCount = new AtomicInteger(0);
        public final AtomicInteger hourlyCount = new AtomicInteger(0);
        public final AtomicInteger totalCount = new AtomicInteger(0);
        public volatile long lastRequestTime = 0;

        public Map<String, Object> toMap() {
            return Map.of(
                "dailyCount", dailyCount.get(),
                "hourlyCount", hourlyCount.get(),
                "totalCount", totalCount.get(),
                "lastRequestTime", lastRequestTime
            );
        }
    }
}
