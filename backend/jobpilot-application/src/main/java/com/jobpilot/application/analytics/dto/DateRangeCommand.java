package com.jobpilot.application.analytics.dto;

import com.jobpilot.common.exception.ValidationException;

import java.time.Instant;

public record DateRangeCommand(Instant startDate, Instant endDate) {
    public DateRangeCommand {
        if (startDate == null) throw new ValidationException("startDate", "Start date must not be null");
        if (endDate == null) throw new ValidationException("endDate", "End date must not be null");
    }
}
