package com.jobpilot.application.interview.dto;

import com.jobpilot.common.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public record ScheduleInterviewCommand(
    UUID userId, UUID companyId, UUID jobId, String type, Instant scheduledAt,
    Integer durationMinutes, String interviewerName, Integer interviewRound,
    String location, String meetingLink, String notes
) {
    public ScheduleInterviewCommand {
        if (userId == null) throw new ValidationException("userId", "User ID must not be null");
        if (type == null || type.isBlank()) throw new ValidationException("type", "Type must not be blank");
        if (scheduledAt == null) throw new ValidationException("scheduledAt", "Scheduled at must not be null");
    }
}
