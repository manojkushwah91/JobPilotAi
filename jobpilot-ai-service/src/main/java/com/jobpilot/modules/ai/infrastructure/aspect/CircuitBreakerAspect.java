package com.jobpilot.modules.ai.infrastructure.aspect;

import com.jobpilot.modules.ai.application.service.AiServiceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
public class CircuitBreakerAspect {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerAspect.class);

    private final Map<String, CircuitState> states = new ConcurrentHashMap<>();

    @Around("execution(* com.jobpilot.modules.ai.domain.port.AIProviderPort.generateText(..)) " +
        "&& bean(*Adapter)")
    public Object circuitBreaker(ProceedingJoinPoint pjp) throws Throwable {
        var providerName = extractProviderName(pjp);
        var state = states.computeIfAbsent(providerName, k -> new CircuitState());

        if (state.isOpen()) {
            if (state.attemptReset()) {
                log.info("Circuit breaker HALF_OPEN for provider: {}", providerName);
                return executeAndTrack(pjp, providerName, state);
            }
            log.warn("Circuit breaker OPEN for provider: {}, failing fast", providerName);
            throw new AiServiceException(providerName + " is unavailable (circuit open)");
        }

        return executeAndTrack(pjp, providerName, state);
    }

    private Object executeAndTrack(ProceedingJoinPoint pjp, String provider, CircuitState state) throws Throwable {
        try {
            var result = pjp.proceed();
            state.recordSuccess();
            return result;
        } catch (Exception e) {
            state.recordFailure();
            log.warn("Provider {} failed (failures={}/{})", provider,
                state.consecutiveFailures.get(), state.failureThreshold);
            throw e;
        }
    }

    private String extractProviderName(ProceedingJoinPoint pjp) {
        var name = pjp.getTarget().getClass().getSimpleName();
        return name.replace("Adapter", "").toLowerCase();
    }

    static class CircuitState {
        private static final int FAILURE_THRESHOLD = 5;
        private static final long OPEN_DURATION_MS = 30_000;
        private static final long HALF_OPEN_TIMEOUT_MS = 5_000;

        final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        final int failureThreshold = FAILURE_THRESHOLD;
        volatile boolean open = false;
        volatile long openedAt = 0;

        boolean isOpen() {
            if (!open) return false;
            if (System.currentTimeMillis() - openedAt > OPEN_DURATION_MS) {
                return false;
            }
            return true;
        }

        boolean attemptReset() {
            if (!open) return true;
            if (System.currentTimeMillis() - openedAt > OPEN_DURATION_MS) {
                open = false;
                consecutiveFailures.set(0);
                return true;
            }
            return false;
        }

        void recordSuccess() {
            open = false;
            consecutiveFailures.set(0);
        }

        void recordFailure() {
            var count = consecutiveFailures.incrementAndGet();
            if (count >= FAILURE_THRESHOLD) {
                open = true;
                openedAt = System.currentTimeMillis();
                log.error("Circuit breaker OPEN for provider after {} consecutive failures", count);
            }
        }
    }
}
