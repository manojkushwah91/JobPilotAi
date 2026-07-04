package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Mission extends BaseAggregateRoot {

    private MissionId missionId;
    private UUID userId;
    private String title;
    private String targetRole;
    private String targetLocation;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private List<String> preferredCompanies;
    private List<String> avoidCompanies;
    private List<String> preferredSkills;
    private String experienceLevel;
    private String employmentType;
    private Integer dailyApplicationLimit;
    private Integer deadlineDays;
    private MissionStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private Instant deadlineAt;
    private int totalJobsFound;
    private int totalApplicationsSubmitted;
    private int totalRejected;
    private int totalPending;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;

    private Mission(MissionId missionId, UUID userId, String title, String targetRole) {
        super(missionId.value());
        this.missionId = missionId;
        this.userId = userId;
        this.title = title;
        this.targetRole = targetRole;
        this.status = MissionStatus.CREATED;
        this.preferredCompanies = new ArrayList<>();
        this.avoidCompanies = new ArrayList<>();
        this.preferredSkills = new ArrayList<>();
        this.dailyApplicationLimit = 20;
        this.deadlineDays = 90;
        this.totalJobsFound = 0;
        this.totalApplicationsSubmitted = 0;
        this.totalRejected = 0;
        this.totalPending = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Mission create(UUID userId, String title, String targetRole) {
        return new Mission(MissionId.generate(), userId, title, targetRole);
    }

    public Mission withTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withSalaryRange(Integer salaryMin, Integer salaryMax, String currency) {
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.currency = currency;
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withPreferredCompanies(List<String> companies) {
        this.preferredCompanies = companies != null ? new ArrayList<>(companies) : new ArrayList<>();
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withAvoidCompanies(List<String> companies) {
        this.avoidCompanies = companies != null ? new ArrayList<>(companies) : new ArrayList<>();
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withPreferredSkills(List<String> skills) {
        this.preferredSkills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withEmploymentType(String employmentType) {
        this.employmentType = employmentType;
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withDailyApplicationLimit(Integer limit) {
        this.dailyApplicationLimit = limit;
        this.updatedAt = Instant.now();
        return this;
    }

    public Mission withDeadlineDays(Integer days) {
        this.deadlineDays = days;
        this.updatedAt = Instant.now();
        return this;
    }

    public static Mission reconstitute(MissionId missionId, UUID userId, String title, String targetRole,
                                        String targetLocation, Integer salaryMin, Integer salaryMax, String currency,
                                        List<String> preferredCompanies, List<String> avoidCompanies,
                                        List<String> preferredSkills, String experienceLevel, String employmentType,
                                        Integer dailyApplicationLimit, Integer deadlineDays, MissionStatus status,
                                        Instant startedAt, Instant completedAt, Instant deadlineAt,
                                        int totalJobsFound, int totalApplicationsSubmitted, int totalRejected,
                                        int totalPending, Map<String, Object> metadata,
                                        Instant createdAt, Instant updatedAt) {
        var m = new Mission(missionId, userId, title, targetRole);
        m.targetLocation = targetLocation;
        m.salaryMin = salaryMin;
        m.salaryMax = salaryMax;
        m.currency = currency;
        m.preferredCompanies = preferredCompanies != null ? new ArrayList<>(preferredCompanies) : new ArrayList<>();
        m.avoidCompanies = avoidCompanies != null ? new ArrayList<>(avoidCompanies) : new ArrayList<>();
        m.preferredSkills = preferredSkills != null ? new ArrayList<>(preferredSkills) : new ArrayList<>();
        m.experienceLevel = experienceLevel;
        m.employmentType = employmentType;
        m.dailyApplicationLimit = dailyApplicationLimit;
        m.deadlineDays = deadlineDays;
        m.status = status;
        m.startedAt = startedAt;
        m.completedAt = completedAt;
        m.deadlineAt = deadlineAt;
        m.totalJobsFound = totalJobsFound;
        m.totalApplicationsSubmitted = totalApplicationsSubmitted;
        m.totalRejected = totalRejected;
        m.totalPending = totalPending;
        m.metadata = metadata;
        m.createdAt = createdAt;
        m.updatedAt = updatedAt;
        return m;
    }

    public void start() {
        if (status != MissionStatus.CREATED) {
            throw new IllegalStateException("Mission can only be started from CREATED status");
        }
        this.status = MissionStatus.ACTIVE;
        this.startedAt = Instant.now();
        this.deadlineAt = Instant.now().plus(java.time.Duration.ofDays(deadlineDays));
        this.updatedAt = Instant.now();
    }

    public void pause() {
        if (status != MissionStatus.ACTIVE) {
            throw new IllegalStateException("Mission can only be paused from ACTIVE status");
        }
        this.status = MissionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public void resume() {
        if (status != MissionStatus.PAUSED) {
            throw new IllegalStateException("Mission can only be resumed from PAUSED status");
        }
        this.status = MissionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = MissionStatus.CANCELLED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void incrementJobsFound() {
        this.totalJobsFound++;
        this.updatedAt = Instant.now();
    }

    public void incrementApplicationsSubmitted() {
        this.totalApplicationsSubmitted++;
        this.updatedAt = Instant.now();
    }

    public void incrementRejected() {
        this.totalRejected++;
        this.updatedAt = Instant.now();
    }

    public void incrementPending() {
        this.totalPending++;
        this.updatedAt = Instant.now();
    }

    public boolean hasReachedDailyLimit() {
        return totalApplicationsSubmitted >= dailyApplicationLimit;
    }

    public boolean isExpired() {
        return deadlineAt != null && Instant.now().isAfter(deadlineAt);
    }

    public boolean shouldStop() {
        return hasReachedDailyLimit() || isExpired() || status != MissionStatus.ACTIVE;
    }

    public MissionId missionId() { return missionId; }
    public UUID userId() { return userId; }
    public String title() { return title; }
    public String targetRole() { return targetRole; }
    public String targetLocation() { return targetLocation; }
    public Integer salaryMin() { return salaryMin; }
    public Integer salaryMax() { return salaryMax; }
    public String currency() { return currency; }
    public List<String> preferredCompanies() { return List.copyOf(preferredCompanies); }
    public List<String> avoidCompanies() { return List.copyOf(avoidCompanies); }
    public List<String> preferredSkills() { return List.copyOf(preferredSkills); }
    public String experienceLevel() { return experienceLevel; }
    public String employmentType() { return employmentType; }
    public Integer dailyApplicationLimit() { return dailyApplicationLimit; }
    public Integer deadlineDays() { return deadlineDays; }
    public MissionStatus status() { return status; }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }
    public Instant deadlineAt() { return deadlineAt; }
    public int totalJobsFound() { return totalJobsFound; }
    public int totalApplicationsSubmitted() { return totalApplicationsSubmitted; }
    public int totalRejected() { return totalRejected; }
    public int totalPending() { return totalPending; }
    public Map<String, Object> metadata() { return metadata; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
