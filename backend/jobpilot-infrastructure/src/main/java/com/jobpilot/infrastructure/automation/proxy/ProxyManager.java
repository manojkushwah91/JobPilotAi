package com.jobpilot.infrastructure.automation.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProxyManager {

    private static final Logger log = LoggerFactory.getLogger(ProxyManager.class);

    private final ConcurrentLinkedQueue<String> proxyPool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final Map<String, ProxyStats> proxyStats = new LinkedHashMap<>();

    @Value("${jobpilot.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${jobpilot.proxy.list:}")
    private String proxyList;

    @Value("${jobpilot.proxy.rotation-interval-ms:300000}")
    private long rotationIntervalMs;

    @Value("${jobpilot.proxy.max-failures:3}")
    private int maxFailures;

    private long lastRotationTime = 0;

    public void initialize() {
        if (!proxyEnabled || proxyList == null || proxyList.isEmpty()) {
            log.info("Proxy rotation disabled or no proxies configured");
            return;
        }

        var proxies = proxyList.split(",");
        for (var proxy : proxies) {
            var trimmed = proxy.trim();
            if (!trimmed.isEmpty()) {
                proxyPool.add(trimmed);
                proxyStats.put(trimmed, new ProxyStats());
            }
        }
        log.info("Initialized proxy pool with {} proxies", proxyPool.size());
    }

    public String getNextProxy() {
        if (!proxyEnabled || proxyPool.isEmpty()) {
            return null;
        }

        var now = System.currentTimeMillis();
        if (now - lastRotationTime > rotationIntervalMs) {
            log.debug("Rotation interval reached, cycling proxies");
            lastRotationTime = now;
        }

        var proxies = new ArrayList<>(proxyPool);
        for (int i = 0; i < proxies.size(); i++) {
            var proxy = proxies.get((currentIndex.getAndIncrement() % proxies.size() + proxies.size()) % proxies.size());
            var stats = proxyStats.get(proxy);
            if (stats != null && stats.failures < maxFailures) {
                return proxy;
            }
        }

        log.warn("All proxies exhausted or at max failures, returning null");
        return null;
    }

    public void reportSuccess(String proxy) {
        if (proxy == null) return;
        var stats = proxyStats.get(proxy);
        if (stats != null) {
            stats.successes++;
            stats.lastUsed = System.currentTimeMillis();
        }
    }

    public void reportFailure(String proxy) {
        if (proxy == null) return;
        var stats = proxyStats.get(proxy);
        if (stats != null) {
            stats.failures++;
            stats.lastUsed = System.currentTimeMillis();
            if (stats.failures >= maxFailures) {
                log.warn("Proxy {} reached max failures ({}), excluding from rotation", proxy, maxFailures);
            }
        }
    }

    public void resetFailures(String proxy) {
        if (proxy == null) return;
        var stats = proxyStats.get(proxy);
        if (stats != null) {
            stats.failures = 0;
        }
    }

    public int getPoolSize() {
        return proxyPool.size();
    }

    public boolean isEnabled() {
        return proxyEnabled && !proxyPool.isEmpty();
    }

    public Map<String, ProxyStats> getStats() {
        return Collections.unmodifiableMap(proxyStats);
    }

    public List<String> getActiveProxies() {
        return proxyPool.stream()
            .filter(p -> {
                var stats = proxyStats.get(p);
                return stats == null || stats.failures < maxFailures;
            })
            .toList();
    }

    public static class ProxyStats {
        public int successes = 0;
        public int failures = 0;
        public long lastUsed = 0;

        public String toString() {
            return String.format("successes=%d, failures=%d, lastUsed=%d",
                successes, failures, lastUsed > 0 ? lastUsed : 0);
        }
    }
}
