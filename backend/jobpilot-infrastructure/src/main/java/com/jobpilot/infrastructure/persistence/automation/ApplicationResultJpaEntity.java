package com.jobpilot.infrastructure.persistence.automation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_results")
public class ApplicationResultJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "job_url", nullable = false)
    private String jobUrl;

    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ApplicationResultJpaEntity() {}

    public ApplicationResultJpaEntity(String id, String sessionId, String jobUrl, String outcome) {
        this.id = id;
        this.sessionId = sessionId;
        this.jobUrl = jobUrl;
        this.outcome = outcome;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
