package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseAggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutomationSession extends BaseAggregateRoot {

    private AutomationSessionId sessionId;
    private UUID userId;
    private UUID applicationId;
    private AutomationStatus status;
    private String currentStep;
    private int progress;
    private List<String> screenshots;
    private String errorMessage;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;

    private AutomationSession() {
        super();
    }

    private AutomationSession(AutomationSessionId sessionId, UUID userId, UUID applicationId) {
        super(sessionId.value());
        this.sessionId = sessionId;
        this.userId = userId;
        this.applicationId = applicationId;
        this.status = AutomationStatus.QUEUED;
        this.progress = 0;
        this.screenshots = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public static AutomationSession start(AutomationSessionId sessionId, UUID userId, UUID applicationId) {
        return new AutomationSession(sessionId, userId, applicationId);
    }

    public static AutomationSession reconstitute(AutomationSessionId sessionId, UUID userId, UUID applicationId,
                                                   AutomationStatus status, String currentStep, int progress,
                                                   List<String> screenshots, String errorMessage,
                                                   Instant startedAt, Instant completedAt, Instant createdAt) {
        var s = new AutomationSession();
        s.sessionId = sessionId;
        s.userId = userId;
        s.applicationId = applicationId;
        s.status = status;
        s.currentStep = currentStep;
        s.progress = progress;
        s.screenshots = screenshots != null ? new ArrayList<>(screenshots) : new ArrayList<>();
        s.errorMessage = errorMessage;
        s.startedAt = startedAt;
        s.completedAt = completedAt;
        s.createdAt = createdAt;
        return s;
    }

    public void updateProgress(int progress, String step) {
        if (status == AutomationStatus.FAILED || status == AutomationStatus.CANCELLED) return;
        if (progress < 0 || progress > 100) throw new IllegalArgumentException("Progress must be 0-100");
        this.progress = progress;
        this.currentStep = step;
        if (status == AutomationStatus.QUEUED) {
            this.status = AutomationStatus.RUNNING;
            this.startedAt = Instant.now();
        }
    }

    public void addScreenshot(String url) {
        screenshots.add(url);
    }

    public void requestConfirmation() {
        this.status = AutomationStatus.AWAITING_CONFIRMATION;
        this.currentStep = "awaiting_confirmation";
    }

    public void confirm() {
        if (this.status != AutomationStatus.AWAITING_CONFIRMATION) return;
        this.status = AutomationStatus.RUNNING;
    }

    public void fail(String error) {
        this.status = AutomationStatus.FAILED;
        this.errorMessage = error;
        this.completedAt = Instant.now();
    }

    public void complete() {
        this.status = AutomationStatus.COMPLETED;
        this.progress = 100;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        if (this.status == AutomationStatus.COMPLETED || this.status == AutomationStatus.FAILED) return;
        this.status = AutomationStatus.CANCELLED;
        this.completedAt = Instant.now();
    }

    public AutomationSessionId sessionId() { return sessionId; }
    public UUID userId() { return userId; }
    public UUID applicationId() { return applicationId; }
    public AutomationStatus status() { return status; }
    public String currentStep() { return currentStep; }
    public int progress() { return progress; }
    public List<String> screenshots() { return List.copyOf(screenshots); }
    public String errorMessage() { return errorMessage; }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }
    public Instant createdAt() { return createdAt; }
}
