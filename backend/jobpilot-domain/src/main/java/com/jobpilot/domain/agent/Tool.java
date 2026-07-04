package com.jobpilot.domain.agent;

import java.util.Map;

public interface Tool {

    String name();

    String description();

    Map<String, Object> execute(Map<String, Object> input);

    default boolean requiresApproval() {
        return false;
    }

    default int timeoutSeconds() {
        return 30;
    }
}
