package com.jobpilot.application.resume.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ResumeResponse(
    String id,
    String userId,
    String title,
    Integer atsScore,
    Map<String, Object> atsScoreData,
    int version,
    boolean isDefault,
    List<SectionDto> sections,
    Instant createdAt,
    Instant updatedAt
) {}
