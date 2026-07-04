package com.jobpilot.interfaces.rest.v1.agent;

import com.jobpilot.domain.agent.Mission;
import com.jobpilot.domain.agent.MissionStatus;

import java.time.Instant;
import java.util.List;

public record MissionResponse(
    String id,
    String userId,
    String title,
    String targetRole,
    String targetLocation,
    Integer salaryMin,
    Integer salaryMax,
    String currency,
    List<String> preferredCompanies,
    List<String> avoidCompanies,
    List<String> preferredSkills,
    String experienceLevel,
    String employmentType,
    Integer dailyApplicationLimit,
    Integer deadlineDays,
    MissionStatus status,
    Instant startedAt,
    Instant completedAt,
    Instant deadlineAt,
    int totalJobsFound,
    int totalApplicationsSubmitted,
    int totalRejected,
    int totalPending,
    Instant createdAt,
    Instant updatedAt
) {
    public static MissionResponse from(Mission mission) {
        return new MissionResponse(
            mission.missionId().value().toString(),
            mission.userId().toString(),
            mission.title(),
            mission.targetRole(),
            mission.targetLocation(),
            mission.salaryMin(),
            mission.salaryMax(),
            mission.currency(),
            mission.preferredCompanies(),
            mission.avoidCompanies(),
            mission.preferredSkills(),
            mission.experienceLevel(),
            mission.employmentType(),
            mission.dailyApplicationLimit(),
            mission.deadlineDays(),
            mission.status(),
            mission.startedAt(),
            mission.completedAt(),
            mission.deadlineAt(),
            mission.totalJobsFound(),
            mission.totalApplicationsSubmitted(),
            mission.totalRejected(),
            mission.totalPending(),
            mission.createdAt(),
            mission.updatedAt()
        );
    }
}
