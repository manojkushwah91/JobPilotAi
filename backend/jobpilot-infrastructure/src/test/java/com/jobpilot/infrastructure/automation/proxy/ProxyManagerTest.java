package com.jobpilot.infrastructure.automation.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ProxyManagerTest {

    private ProxyManager proxyManager;

    @BeforeEach
    void setUp() throws Exception {
        proxyManager = new ProxyManager();
        setField(proxyManager, "proxyEnabled", false);
        setField(proxyManager, "proxyList", "");
        setField(proxyManager, "rotationIntervalMs", 300000L);
        setField(proxyManager, "maxFailures", 3);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Should not be enabled when disabled")
    void shouldNotBeEnabled() {
        assertFalse(proxyManager.isEnabled());
    }

    @Test
    @DisplayName("Should return null proxy when disabled")
    void shouldReturnNullWhenDisabled() {
        assertNull(proxyManager.getNextProxy());
    }

    @Test
    @DisplayName("Should initialize with proxy list")
    void shouldInitializeWithProxyList() throws Exception {
        setField(proxyManager, "proxyEnabled", true);
        setField(proxyManager, "proxyList", "http://proxy1:8080,http://proxy2:8080,http://proxy3:8080");

        proxyManager.initialize();

        assertTrue(proxyManager.isEnabled());
        assertEquals(3, proxyManager.getPoolSize());
    }

    @Test
    @DisplayName("Should return proxies in round-robin")
    void shouldReturnProxiesRoundRobin() throws Exception {
        setField(proxyManager, "proxyEnabled", true);
        setField(proxyManager, "proxyList", "http://proxy1:8080,http://proxy2:8080");

        proxyManager.initialize();

        var proxy1 = proxyManager.getNextProxy();
        var proxy2 = proxyManager.getNextProxy();
        var proxy3 = proxyManager.getNextProxy();

        assertNotNull(proxy1);
        assertNotNull(proxy2);
        assertNotNull(proxy3);
        assertEquals(proxy1, proxy3);
        assertNotEquals(proxy1, proxy2);
    }

    @Test
    @DisplayName("Should report success")
    void shouldReportSuccess() throws Exception {
        setField(proxyManager, "proxyEnabled", true);
        setField(proxyManager, "proxyList", "http://proxy1:8080");

        proxyManager.initialize();
        proxyManager.reportSuccess("http://proxy1:8080");

        var stats = proxyManager.getStats();
        assertEquals(1, stats.get("http://proxy1:8080").successes);
        assertEquals(0, stats.get("http://proxy1:8080").failures);
    }

    @Test
    @DisplayName("Should report failure and exclude after max failures")
    void shouldExcludeAfterMaxFailures() throws Exception {
        setField(proxyManager, "proxyEnabled", true);
        setField(proxyManager, "proxyList", "http://proxy1:8080");
        setField(proxyManager, "maxFailures", 2);

        proxyManager.initialize();

        proxyManager.reportFailure("http://proxy1:8080");
        proxyManager.reportFailure("http://proxy1:8080");

        var activeProxies = proxyManager.getActiveProxies();
        assertTrue(activeProxies.isEmpty());
    }

    @Test
    @DisplayName("Should reset failures")
    void shouldResetFailures() throws Exception {
        setField(proxyManager, "proxyEnabled", true);
        setField(proxyManager, "proxyList", "http://proxy1:8080");
        setField(proxyManager, "maxFailures", 2);

        proxyManager.initialize();

        proxyManager.reportFailure("http://proxy1:8080");
        proxyManager.reportFailure("http://proxy1:8080");
        assertTrue(proxyManager.getActiveProxies().isEmpty());

        proxyManager.resetFailures("http://proxy1:8080");
        assertEquals(1, proxyManager.getActiveProxies().size());
    }

    @Test
    @DisplayName("Should handle null proxy gracefully")
    void shouldHandleNullProxy() {
        proxyManager.reportSuccess(null);
        proxyManager.reportFailure(null);
        proxyManager.resetFailures(null);
    }

    @Test
    @DisplayName("Should have empty stats when no proxies")
    void shouldHaveEmptyStats() {
        var stats = proxyManager.getStats();
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }
}
