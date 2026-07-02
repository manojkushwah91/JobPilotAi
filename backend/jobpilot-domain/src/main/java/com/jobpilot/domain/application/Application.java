package com.jobpilot.domain.application;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.resume.ResumeId;
import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application extends BaseAggregateRoot {

    private ApplicationId applicationId;
    private UserId userId;
    private JobId jobListingId;
    private ResumeId resumeId;
    private ApplicationId coverLetterId;
    private ApplicationStatus status;
    private List<Map<String, Object>> statusHistory;
    private Map<String, Object> automationInfo;
    private Map<String, Object> salaryOffered;
    private Instant appliedAt;
    private boolean deleted;
    private Instant deletedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Application(ApplicationId applicationId, UserId userId, JobId jobListingId) {
        super(applicationId.value());
        this.applicationId = applicationId;
        this.userId = userId;
        this.jobListingId = jobListingId;
        this.status = ApplicationStatus.SAVED;
        this.statusHistory = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Application create(ApplicationId applicationId, UserId userId, JobId jobListingId) {
        var app = new Application(applicationId, userId, jobListingId);
        app.statusHistory.add(Map.of(
            "status", "SAVED", "timestamp", Instant.now().toString()
        ));
        return app;
    }

    public static Application reconstitute(ApplicationId applicationId, UserId userId, JobId jobListingId,
                                            ResumeId resumeId, ApplicationId coverLetterId,
                                            ApplicationStatus status, List<Map<String, Object>> statusHistory,
                                            Map<String, Object> automationInfo, Map<String, Object> salaryOffered,
                                            Instant appliedAt, boolean deleted, Instant deletedAt,
                                            Instant createdAt, Instant updatedAt) {
        var app = new Application(applicationId, userId, jobListingId);
        app.resumeId = resumeId;
        app.coverLetterId = coverLetterId;
        app.status = status;
        app.statusHistory = statusHistory != null ? new ArrayList<>(statusHistory) : new ArrayList<>();
        app.automationInfo = automationInfo;
        app.salaryOffered = salaryOffered;
        app.appliedAt = appliedAt;
        app.deleted = deleted;
        app.deletedAt = deletedAt;
        return app;
    }

    public void submit(ResumeId resumeId) {
        this.resumeId = resumeId;
        this.status = ApplicationStatus.APPLIED;
        this.appliedAt = Instant.now();
        this.updatedAt = Instant.now();
        addStatusChange("APPLIED");
    }

    public void updateStatus(ApplicationStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
        addStatusChange(newStatus.name());
    }

    public void softDelete() {
        if (deleted) return;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void addStatusChange(String statusStr) {
        statusHistory.add(Map.of("status", statusStr, "timestamp", Instant.now().toString()));
    }

    public ApplicationId applicationId() { return applicationId; }
    public UserId userId() { return userId; }
    public JobId jobListingId() { return jobListingId; }
    public ResumeId resumeId() { return resumeId; }
    public ApplicationId coverLetterId() { return coverLetterId; }
    public ApplicationStatus status() { return status; }
    public List<Map<String, Object>> statusHistory() { return List.copyOf(statusHistory); }
    public Map<String, Object> automationInfo() { return automationInfo; }
    public Map<String, Object> salaryOffered() { return salaryOffered; }
    public Instant appliedAt() { return appliedAt; }
    public boolean isDeleted() { return deleted; }
    public Instant deletedAt() { return deletedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
