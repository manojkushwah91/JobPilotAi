package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.RetryManagerPort;
import com.microsoft.playwright.PlaywrightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

@Component
public class DefaultRetryManager implements RetryManagerPort {

    private static final Logger log = LoggerFactory.getLogger(DefaultRetryManager.class);

    private static final Set<Class<? extends Exception>> RETRYABLE_EXCEPTIONS = Set.of(
        PlaywrightException.class,
        RuntimeException.class
    );

    @Override
    public <T> T executeWithRetry(Supplier<T> action, String actionName, int maxRetries, int delayMs) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.debug("Executing {} (attempt {}/{})", actionName, attempt, maxRetries);
                return action.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for {}: {}", attempt, actionName, e.getMessage());

                if (attempt < maxRetries && shouldRetry(e, attempt)) {
                    var delay = calculateDelay(attempt, delayMs);
                    log.debug("Retrying {} in {}ms", actionName, delay);
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }

        throw new RuntimeException("Failed after " + maxRetries + " attempts: " + actionName, lastException);
    }

    @Override
    public boolean shouldRetry(Exception error, int attemptCount) {
        if (attemptCount >= 3) return false;

        return RETRYABLE_EXCEPTIONS.stream().anyMatch(retryable ->
            retryable.isInstance(error) ||
            (error.getCause() != null && retryable.isInstance(error.getCause()))
        );
    }

    @Override
    public int calculateDelay(int attemptCount, int baseDelayMs) {
        return baseDelayMs * (int) Math.pow(2, attemptCount - 1);
    }
}
