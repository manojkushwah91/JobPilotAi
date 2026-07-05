package com.jobpilot.application.automation.ports;

import java.util.function.Supplier;

public interface RetryManagerPort {

    <T> T executeWithRetry(Supplier<T> action, String actionName, int maxRetries, int delayMs);

    boolean shouldRetry(Exception error, int attemptCount);

    int calculateDelay(int attemptCount, int baseDelayMs);

    default <T> T executeWithExponentialBackoff(Supplier<T> action, String actionName, int maxRetries) {
        return executeWithRetry(action, actionName, maxRetries, 1000);
    }
}
