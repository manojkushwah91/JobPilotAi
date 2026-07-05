package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ApplicationResult extends BaseAggregateRoot {

    private ResultId resultId;
    private UUID userId;
    private UUID missionId;
    private UUID sessionId;
    private String jobUrl;
    private String jobTitle;
    private String companyName;
    private ApplicationOutcome outcome;
    private String resumeVersion;
    private String coverLetterVersion;
    private Map<String, Object> formData;
    private byte[] screenshotBefore;
    private byte[] screenshotAfter;
    private String errorMessage;
    private int attemptCount;
    private Instant appliedAt;
    private final Instant createdAt;

    private ApplicationResult(ResultId resultId, UUID userId, UUID missionId, UUID sessionId) {
        super(resultId.value());
        this.resultId = resultId;
        this.userId = userId;
        this.missionId = missionId;
        this.sessionId = sessionId;
        this.outcome = ApplicationOutcome.PENDING;
        this.attemptCount = 0;
        this.createdAt = Instant.now();
    }

    public static ApplicationResult create(UUID userId, UUID missionId, UUID sessionId) {
        return new ApplicationResult(ResultId.generate(), userId, missionId, sessionId);
    }

    public void markSubmitted() {
        this.outcome = ApplicationOutcome.SUBMITTED;
        this.appliedAt = Instant.now();
    }

    public void markSuccess() {
        this.outcome = ApplicationOutcome.SUCCESS;
        this.appliedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.outcome = ApplicationOutcome.FAILED;
        this.errorMessage = error;
    }

    public void markRejected(String reason) {
        this.outcome = ApplicationOutcome.REJECTED;
        this.errorMessage = reason;
    }

    public void markRequiresCaptcha() {
        this.outcome = ApplicationOutcome.REQUIRES_CAPTCHA;
    }

    public void markRequiresApproval() {
        this.outcome = ApplicationOutcome.REQUIRES_APPROVAL;
    }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public ResultId resultId() { return resultId; }
    public UUID userId() { return userId; }
    public UUID missionId() { return missionId; }
    public UUID sessionId() { return sessionId; }
    public String jobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
    public String jobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String companyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public ApplicationOutcome outcome() { return outcome; }
    public String resumeVersion() { return resumeVersion; }
    public void setResumeVersion(String resumeVersion) { this.resumeVersion = resumeVersion; }
    public String coverLetterVersion() { return coverLetterVersion; }
    public void setCoverLetterVersion(String coverLetterVersion) { this.coverLetterVersion = coverLetterVersion; }
    public Map<String, Object> formData() { return formData; }
    public void setFormData(Map<String, Object> formData) { this.formData = formData; }
    public byte[] screenshotBefore() { return screenshotBefore; }
    public void setScreenshotBefore(byte[] screenshot) { this.screenshotBefore = screenshot; }
    public byte[] screenshotAfter() { return screenshotAfter; }
    public void setScreenshotAfter(byte[] screenshot) { this.screenshotAfter = screenshot; }
    public String errorMessage() { return errorMessage; }
    public int attemptCount() { return attemptCount; }
    public Instant appliedAt() { return appliedAt; }
    public Instant createdAt() { return createdAt; }
}
