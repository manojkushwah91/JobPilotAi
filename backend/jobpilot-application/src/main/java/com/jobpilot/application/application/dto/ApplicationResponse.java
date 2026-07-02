package com.jobpilot.application.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ApplicationResponse(
    String id,
    String userId,
    String jobListingId,
    String resumeId,
    String coverLetterId,
    String status,
    List<Map<String, Object>> statusHistory,
    Map<String, Object> automationInfo,
    Map<String, Object> salaryOffered,
    Instant appliedAt,
    Instant createdAt,
    Instant updatedAt
) {}
