package com.jobpilot.common.util;

import java.util.UUID;

public final class IdGenerator {
    private IdGenerator() {}

    public static UUID newId() {
        return UUID.randomUUID();
    }

    public static String newIdString() {
        return newId().toString();
    }

    public static UUID fromString(String id) {
        return UUID.fromString(id);
    }
}
