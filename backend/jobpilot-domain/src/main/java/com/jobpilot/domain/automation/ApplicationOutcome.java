package com.jobpilot.domain.automation;

public enum ApplicationOutcome {
    PENDING,
    SUBMITTED,
    SUCCESS,
    FAILED,
    REJECTED,
    REQUIRES_CAPTCHA,
    REQUIRES_APPROVAL,
    DUPLICATE,
    UNAVAILABLE
}
