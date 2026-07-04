package com.jobpilot.infrastructure.persistence.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "missions")
public class MissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "target_role", nullable = false)
    private String targetRole;

    @Column(name = "target_location")
    private String targetLocation;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    private String currency;

    @Column(name = "preferred_companies")
    private String preferredCompanies;

    @Column(name = "avoid_companies")
    private String avoidCompanies;

    @Column(name = "preferred_skills")
    private String preferredSkills;

    @Column(name = "experience_level")
    private String experienceLevel;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "daily_application_limit")
    private Integer dailyApplicationLimit;

    @Column(name = "deadline_days")
    private Integer deadlineDays;

    @Enumerated(EnumType.STRING)
    private com.jobpilot.domain.agent.MissionStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "deadline_at")
    private Instant deadlineAt;

    @Column(name = "total_jobs_found")
    private int totalJobsFound;

    @Column(name = "total_applications_submitted")
    private int totalApplicationsSubmitted;

    @Column(name = "total_rejected")
    private int totalRejected;

    @Column(name = "total_pending")
    private int totalPending;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MissionJpaEntity() {}

    public static MissionJpaEntity fromDomain(com.jobpilot.domain.agent.Mission mission) {
        var entity = new MissionJpaEntity();
        entity.id = mission.missionId().value();
        entity.userId = mission.userId();
        entity.title = mission.title();
        entity.targetRole = mission.targetRole();
        entity.targetLocation = mission.targetLocation();
        entity.salaryMin = mission.salaryMin();
        entity.salaryMax = mission.salaryMax();
        entity.currency = mission.currency();
        entity.preferredCompanies = mission.preferredCompanies().toString();
        entity.avoidCompanies = mission.avoidCompanies().toString();
        entity.preferredSkills = mission.preferredSkills().toString();
        entity.experienceLevel = mission.experienceLevel();
        entity.employmentType = mission.employmentType();
        entity.dailyApplicationLimit = mission.dailyApplicationLimit();
        entity.deadlineDays = mission.deadlineDays();
        entity.status = mission.status();
        entity.startedAt = mission.startedAt();
        entity.completedAt = mission.completedAt();
        entity.deadlineAt = mission.deadlineAt();
        entity.totalJobsFound = mission.totalJobsFound();
        entity.totalApplicationsSubmitted = mission.totalApplicationsSubmitted();
        entity.totalRejected = mission.totalRejected();
        entity.totalPending = mission.totalPending();
        entity.createdAt = mission.createdAt();
        entity.updatedAt = mission.updatedAt();
        return entity;
    }

    public com.jobpilot.domain.agent.Mission toDomain() {
        return com.jobpilot.domain.agent.Mission.reconstitute(
            com.jobpilot.domain.agent.MissionId.from(id),
            userId, title, targetRole, targetLocation,
            salaryMin, salaryMax, currency,
            preferredCompanies != null ? parseList(preferredCompanies) : java.util.List.of(),
            avoidCompanies != null ? parseList(avoidCompanies) : java.util.List.of(),
            preferredSkills != null ? parseList(preferredSkills) : java.util.List.of(),
            experienceLevel, employmentType,
            dailyApplicationLimit, deadlineDays, status,
            startedAt, completedAt, deadlineAt,
            totalJobsFound, totalApplicationsSubmitted, totalRejected, totalPending,
            null, createdAt, updatedAt
        );
    }

    private java.util.List<String> parseList(String value) {
        if (value == null || value.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(value.replace("[", "").replace("]", "").split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getTargetLocation() { return targetLocation; }
    public void setTargetLocation(String targetLocation) { this.targetLocation = targetLocation; }
    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }
    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPreferredCompanies() { return preferredCompanies; }
    public void setPreferredCompanies(String preferredCompanies) { this.preferredCompanies = preferredCompanies; }
    public String getAvoidCompanies() { return avoidCompanies; }
    public void setAvoidCompanies(String avoidCompanies) { this.avoidCompanies = avoidCompanies; }
    public String getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(String preferredSkills) { this.preferredSkills = preferredSkills; }
    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public Integer getDailyApplicationLimit() { return dailyApplicationLimit; }
    public void setDailyApplicationLimit(Integer dailyApplicationLimit) { this.dailyApplicationLimit = dailyApplicationLimit; }
    public Integer getDeadlineDays() { return deadlineDays; }
    public void setDeadlineDays(Integer deadlineDays) { this.deadlineDays = deadlineDays; }
    public com.jobpilot.domain.agent.MissionStatus getStatus() { return status; }
    public void setStatus(com.jobpilot.domain.agent.MissionStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(Instant deadlineAt) { this.deadlineAt = deadlineAt; }
    public int getTotalJobsFound() { return totalJobsFound; }
    public void setTotalJobsFound(int totalJobsFound) { this.totalJobsFound = totalJobsFound; }
    public int getTotalApplicationsSubmitted() { return totalApplicationsSubmitted; }
    public void setTotalApplicationsSubmitted(int totalApplicationsSubmitted) { this.totalApplicationsSubmitted = totalApplicationsSubmitted; }
    public int getTotalRejected() { return totalRejected; }
    public void setTotalRejected(int totalRejected) { this.totalRejected = totalRejected; }
    public int getTotalPending() { return totalPending; }
    public void setTotalPending(int totalPending) { this.totalPending = totalPending; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
