package com.jobpilot.common.util;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.assertj.core.api.Assertions.assertThat;

class TimeProviderTest {

    @Test
    void shouldProvideCurrentTime() {
        var provider = TimeProvider.system();
        var before = Instant.now();
        var now = provider.now();
        var after = Instant.now();
        assertThat(now).isBetween(before, after);
    }

    @Test
    void shouldUseCustomClock() {
        var fixed = Instant.parse("2026-06-15T10:00:00Z");
        var clock = Clock.fixed(fixed, ZoneOffset.UTC);
        var provider = new TimeProvider(clock);
        assertThat(provider.now()).isEqualTo(fixed);
    }

    @Test
    void shouldCreateWithClock() {
        var fixed = Instant.parse("2026-06-15T10:00:00Z");
        var clock = Clock.fixed(fixed, ZoneOffset.UTC);
        var provider = TimeProvider.system().withClock(clock);
        assertThat(provider.now()).isEqualTo(fixed);
    }

    @Test
    void shouldNotShareStateBetweenProviders() {
        var fixed = Instant.parse("2026-06-15T10:00:00Z");
        var clock = Clock.fixed(fixed, ZoneOffset.UTC);
        var custom = new TimeProvider(clock);
        var system = TimeProvider.system();

        assertThat(custom.now()).isEqualTo(fixed);
        assertThat(system.now()).isNotEqualTo(fixed);
    }
}
