package com.jobpilot.interfaces.rest.v1.agent;

import java.util.List;

public record CreateMissionRequest(
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
    Integer dailyLimit,
    Integer deadlineDays
) {}
