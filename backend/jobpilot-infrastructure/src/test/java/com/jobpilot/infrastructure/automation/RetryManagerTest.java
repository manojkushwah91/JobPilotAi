package com.jobpilot.infrastructure.automation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RetryManagerTest {

    @InjectMocks
    private DefaultRetryManager retryManager;

    @Test
    void shouldExecuteActionSuccessfully() {
        var result = retryManager.executeWithRetry(
            () -> "success",
            "test action",
            3,
            100
        );

        assertEquals("success", result);
    }

    @Test
    void shouldRetryOnFailure() {
        var attempts = new AtomicInteger(0);

        var result = retryManager.executeWithRetry(
            () -> {
                if (attempts.incrementAndGet() < 3) {
                    throw new RuntimeException("Temporary failure");
                }
                return "success after retry";
            },
            "retry test",
            3,
            100
        );

        assertEquals("success after retry", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void shouldThrowAfterMaxRetries() {
        assertThrows(RuntimeException.class, () -> {
            retryManager.executeWithRetry(
                () -> {
                    throw new RuntimeException("Permanent failure");
                },
                "permanent failure test",
                3,
                100
            );
        });
    }

    @Test
    void shouldCalculateExponentialDelay() {
        assertEquals(1000, retryManager.calculateDelay(1, 1000));
        assertEquals(2000, retryManager.calculateDelay(2, 1000));
        assertEquals(4000, retryManager.calculateDelay(3, 1000));
        assertEquals(8000, retryManager.calculateDelay(4, 1000));
    }

    @Test
    void shouldRetryOnRuntimeException() {
        var exception = new RuntimeException("runtime error");
        assertTrue(retryManager.shouldRetry(exception, 1));
    }

    @Test
    void shouldNotRetryAfterMaxAttempts() {
        var exception = new RuntimeException("runtime error");
        assertFalse(retryManager.shouldRetry(exception, 3));
    }
}
