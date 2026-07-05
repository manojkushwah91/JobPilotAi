package com.jobpilot.infrastructure.automation.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class BoardRateLimiterTest {

    private BoardRateLimiter rateLimiter;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiter = new BoardRateLimiter();
        setField(rateLimiter, "defaultDelayMs", 100);
        setField(rateLimiter, "defaultDailyCap", 5);
        setField(rateLimiter, "defaultHourlyCap", 3);
        rateLimiter.initialize();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Should allow request within limits")
    void shouldAllowWithinLimits() {
        assertTrue(rateLimiter.canProceed("linkedin"));
    }

    @Test
    @DisplayName("Should block after daily cap reached")
    void shouldBlockAfterDailyCap() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordRequest("linkedin");
        }
        assertFalse(rateLimiter.canProceed("linkedin"));
    }

    @Test
    @DisplayName("Should respect delay between requests")
    void shouldRespectDelay() {
        rateLimiter.recordRequest("linkedin");
        assertFalse(rateLimiter.canProceed("linkedin"));
    }

    @Test
    @DisplayName("Should track stats correctly")
    void shouldTrackStats() {
        rateLimiter.recordRequest("linkedin");
        rateLimiter.recordRequest("linkedin");

        var stats = rateLimiter.getStats("linkedin");
        assertEquals(2, stats.dailyCount.get());
        assertEquals(2, stats.totalCount.get());
    }

    @Test
    @DisplayName("Should reset hourly count")
    void shouldResetHourly() {
        rateLimiter.recordRequest("linkedin");
        rateLimiter.recordRequest("linkedin");
        rateLimiter.resetHourly("linkedin");

        var stats = rateLimiter.getStats("linkedin");
        assertEquals(0, stats.hourlyCount.get());
        assertEquals(2, stats.totalCount.get());
    }

    @Test
    @DisplayName("Should reset daily count")
    void shouldResetDaily() {
        rateLimiter.recordRequest("linkedin");
        rateLimiter.resetDaily("linkedin");

        var stats = rateLimiter.getStats("linkedin");
        assertEquals(0, stats.dailyCount.get());
    }

    @Test
    @DisplayName("Should update limits dynamically")
    void shouldUpdateLimits() {
        rateLimiter.setLimits("custom", 5000, 100, 50);
        var limits = rateLimiter.getLimits("custom");

        assertEquals(5000, limits.delayMs);
        assertEquals(100, limits.dailyCap);
        assertEquals(50, limits.hourlyCap);
    }

    @Test
    @DisplayName("Should return default limits for unknown board")
    void shouldReturnDefaultsForUnknown() {
        var limits = rateLimiter.getLimits("unknown_board");
        assertEquals(100, limits.delayMs);
        assertEquals(5, limits.dailyCap);
        assertEquals(3, limits.hourlyCap);
    }

    @Test
    @DisplayName("Should use custom limits per board")
    void shouldUseCustomLimits() {
        rateLimiter.setLimits("indeed", 2000, 30, 10);

        for (int i = 0; i < 10; i++) {
            rateLimiter.recordRequest("indeed");
        }

        assertFalse(rateLimiter.canProceed("indeed"));
    }
}
