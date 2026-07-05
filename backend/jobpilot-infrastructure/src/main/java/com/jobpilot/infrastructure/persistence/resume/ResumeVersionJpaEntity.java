package com.jobpilot.infrastructure.persistence.resume;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resume_versions")
public class ResumeVersionJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "resume_id", nullable = false, length = 36)
    private String resumeId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "job_url")
    private String jobUrl;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tailored_content", columnDefinition = "TEXT")
    private String tailoredContent;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public ResumeVersionJpaEntity() {}

    public static ResumeVersionJpaEntity fromDomain(com.jobpilot.domain.resume.ResumeVersion domain) {
        var entity = new ResumeVersionJpaEntity();
        entity.id = domain.versionId().toString();
        entity.resumeId = domain.resumeId().toString();
        entity.userId = domain.userId().toString();
        entity.jobUrl = domain.jobUrl();
        entity.jobTitle = domain.jobTitle();
        entity.companyName = domain.companyName();
        entity.tailoredContent = domain.tailoredContent();
        entity.createdAt = domain.createdAt();
        return entity;
    }

    public com.jobpilot.domain.resume.ResumeVersion toDomain() {
        return com.jobpilot.domain.resume.ResumeVersion.reconstitute(
            UUID.fromString(id),
            UUID.fromString(resumeId),
            UUID.fromString(userId),
            tailoredContent,
            jobUrl,
            jobTitle,
            companyName,
            null,
            createdAt
        );
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getTailoredContent() { return tailoredContent; }
    public void setTailoredContent(String tailoredContent) { this.tailoredContent = tailoredContent; }
    public Instant getCreatedAt() { return createdAt; }
}
