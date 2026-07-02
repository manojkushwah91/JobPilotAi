package com.jobpilot.application.automation.dto;

import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.automation.AutomationStatus;
import java.time.Instant;
import java.util.List;

public record AutomationSessionResponse(
    String id,
    String userId,
    String applicationId,
    AutomationStatus status,
    String currentStep,
    int progress,
    List<String> screenshots,
    String errorMessage,
    Instant startedAt,
    Instant completedAt,
    Instant createdAt
) {
    public static AutomationSessionResponse from(AutomationSession session) {
        return new AutomationSessionResponse(
            session.sessionId().value().toString(),
            session.userId().toString(),
            session.applicationId().toString(),
            session.status(),
            session.currentStep(),
            session.progress(),
            session.screenshots(),
            session.errorMessage(),
            session.startedAt(),
            session.completedAt(),
            session.createdAt()
        );
    }
}
