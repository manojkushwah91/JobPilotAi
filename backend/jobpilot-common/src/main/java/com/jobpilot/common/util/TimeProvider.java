package com.jobpilot.common.util;

import java.time.Clock;

public class TimeProvider implements ClockProvider {
    private final Clock clock;

    public TimeProvider() {
        this(Clock.systemUTC());
    }

    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public java.time.Instant now() {
        return java.time.Instant.now(clock);
    }

    public static TimeProvider system() {
        return new TimeProvider();
    }

    public TimeProvider withClock(Clock clock) {
        return new TimeProvider(clock);
    }
}
