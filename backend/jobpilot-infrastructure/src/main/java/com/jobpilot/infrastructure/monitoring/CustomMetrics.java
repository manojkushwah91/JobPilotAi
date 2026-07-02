package com.jobpilot.infrastructure.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CustomMetrics {

    private final MeterRegistry registry;
    private final Counter aiTokenCounter;
    private final Counter applicationSubmittedCounter;
    private final Counter resumeUploadCounter;
    private final Counter loginAttemptCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;

    public CustomMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.aiTokenCounter = Counter.builder("jobpilot.ai.tokens")
            .description("Total AI tokens consumed")
            .register(registry);
        this.applicationSubmittedCounter = Counter.builder("jobpilot.applications.submitted")
            .description("Total job applications submitted")
            .register(registry);
        this.resumeUploadCounter = Counter.builder("jobpilot.resumes.uploaded")
            .description("Total resumes uploaded")
            .register(registry);
        this.loginAttemptCounter = Counter.builder("jobpilot.auth.login.attempts")
            .description("Total login attempts")
            .register(registry);
        this.loginSuccessCounter = Counter.builder("jobpilot.auth.login.success")
            .description("Successful logins")
            .register(registry);
        this.loginFailureCounter = Counter.builder("jobpilot.auth.login.failures")
            .description("Failed logins")
            .register(registry);
    }

    public void recordAiTokens(long tokens) { aiTokenCounter.increment(tokens); }
    public void recordApplicationSubmitted() { applicationSubmittedCounter.increment(); }
    public void recordResumeUpload() { resumeUploadCounter.increment(); }
    public void recordLoginAttempt() { loginAttemptCounter.increment(); }
    public void recordLoginSuccess() { loginSuccessCounter.increment(); }
    public void recordLoginFailure() { loginFailureCounter.increment(); }

    public Timer.Sample startTimer() { return Timer.start(registry); }
    public void stopTimer(Timer.Sample sample, String operation) {
        sample.stop(Timer.builder("jobpilot.operation.duration")
            .tag("operation", operation)
            .register(registry));
    }
}
