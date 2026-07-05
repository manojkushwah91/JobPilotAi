package com.jobpilot.infrastructure.automation.retry;

import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class SmartRetryManager {

    private static final Logger log = LoggerFactory.getLogger(SmartRetryManager.class);

    private final Map<String, RetryStrategy> strategies = new ConcurrentHashMap<>();

    public SmartRetryManager() {
        strategies.put("NETWORK_TIMEOUT", new RetryStrategy(3, 2000, 2.0));
        strategies.put("RATE_LIMIT", new RetryStrategy(5, 5000, 3.0));
        strategies.put("CAPTCHA", new RetryStrategy(1, 0, 1.0));
        strategies.put("SESSION_EXPIRED", new RetryStrategy(2, 1000, 1.5));
        strategies.put("ELEMENT_NOT_FOUND", new RetryStrategy(2, 1000, 1.0));
        strategies.put("UNKNOWN", new RetryStrategy(2, 1000, 1.5));
    }

    public <T> T executeWithStrategy(String errorType, Supplier<T> action, String actionName) {
        var strategy = strategies.getOrDefault(errorType, strategies.get("UNKNOWN"));
        return strategy.execute(action, actionName);
    }

    public String classifyError(Exception error) {
        var message = error.getMessage() != null ? error.getMessage().toLowerCase() : "";
        var cause = error.getCause();
        var causeMessage = cause != null && cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";

        if (message.contains("timeout") || causeMessage.contains("timeout")) return "NETWORK_TIMEOUT";
        if (message.contains("rate limit") || message.contains("too many requests")) return "RATE_LIMIT";
        if (message.contains("captcha") || causeMessage.contains("captcha")) return "CAPTCHA";
        if (message.contains("session") || message.contains("login required")) return "SESSION_EXPIRED";
        if (message.contains("element not found") || message.contains("no such element")) return "ELEMENT_NOT_FOUND";

        return "UNKNOWN";
    }

    public void registerStrategy(String errorType, int maxRetries, long baseDelayMs, double backoffMultiplier) {
        strategies.put(errorType, new RetryStrategy(maxRetries, baseDelayMs, backoffMultiplier));
    }

    private static class RetryStrategy {
        private final int maxRetries;
        private final long baseDelayMs;
        private final double backoffMultiplier;

        RetryStrategy(int maxRetries, long baseDelayMs, double backoffMultiplier) {
            this.maxRetries = maxRetries;
            this.baseDelayMs = baseDelayMs;
            this.backoffMultiplier = backoffMultiplier;
        }

        <T> T execute(Supplier<T> action, String actionName) {
            Exception lastException = null;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    log.debug("Executing {} (attempt {}/{})", actionName, attempt, maxRetries);
                    return action.get();
                } catch (Exception e) {
                    lastException = e;
                    log.warn("Attempt {} failed for {}: {}", attempt, actionName, e.getMessage());
                    if (attempt < maxRetries) {
                        var delay = (long) (baseDelayMs * Math.pow(backoffMultiplier, attempt - 1));
                        log.debug("Retrying {} in {}ms", actionName, delay);
                        try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                }
            }
            throw new RuntimeException("Failed after " + maxRetries + " attempts: " + actionName, lastException);
        }
    }
}
