package com.jobpilot.common.util;

import java.time.Instant;

public interface ClockProvider {
    Instant now();
}
