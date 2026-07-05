package com.jobpilot.domain.automation;

public enum SessionStatus {
    CREATED,
    ACTIVE,
    PAUSED,
    WAITING_FOR_CAPTCHA,
    WAITING_FOR_APPROVAL,
    COMPLETED,
    FAILED,
    CANCELLED,
    CLOSED
}
