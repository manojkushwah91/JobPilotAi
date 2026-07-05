package com.jobpilot.infrastructure.persistence.automation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application_results")
public class ApplicationResultJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "mission_id", length = 36)
    private String missionId;

    @Column(name = "job_url", nullable = false)
    private String jobUrl;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ApplicationResultJpaEntity() {}

    public static ApplicationResultJpaEntity createPending(UUID userId, UUID missionId, UUID sessionId) {
        var entity = new ApplicationResultJpaEntity();
        entity.id = UUID.randomUUID().toString();
        entity.userId = userId != null ? userId.toString() : null;
        entity.missionId = missionId != null ? missionId.toString() : null;
        entity.sessionId = sessionId.toString();
        entity.outcome = "PENDING";
        return entity;
    }

    public void markSubmitted() {
        this.outcome = "SUBMITTED";
        this.appliedAt = LocalDateTime.now();
    }

    public void markFailed(String error) {
        this.outcome = "FAILED";
        this.errorMessage = error;
    }

    public void markRequiresCaptcha() {
        this.outcome = "PENDING_CAPTCHA";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMissionId() { return missionId; }
    public void setMissionId(String missionId) { this.missionId = missionId; }

    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
