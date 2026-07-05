package com.jobpilot.domain.automation;

import java.time.Instant;
import java.util.Map;

public record PageAction(
    ActionType type,
    String selector,
    String value,
    String description,
    boolean success,
    String errorMessage,
    byte[] screenshot,
    Instant timestamp,
    Map<String, Object> metadata
) {
    public enum ActionType {
        NAVIGATE,
        CLICK,
        FILL,
        UPLOAD,
        SELECT,
        WAIT,
        SCREENSHOT,
        SCROLL,
        HOVER,
        KEY_PRESS,
        EXTRACT,
        EVALUATE_JS
    }

    public static PageAction navigate(String url) {
        return new PageAction(ActionType.NAVIGATE, null, url, "Navigate to " + url,
            true, null, null, Instant.now(), null);
    }

    public static PageAction click(String selector, String description) {
        return new PageAction(ActionType.CLICK, selector, null, description,
            true, null, null, Instant.now(), null);
    }

    public static PageAction fill(String selector, String value, String description) {
        return new PageAction(ActionType.FILL, selector, value, description,
            true, null, null, Instant.now(), null);
    }

    public static PageAction upload(String selector, String filePath) {
        return new PageAction(ActionType.UPLOAD, selector, filePath, "Upload " + filePath,
            true, null, null, Instant.now(), null);
    }

    public static PageAction wait(String selector, int timeoutSeconds) {
        return new PageAction(ActionType.WAIT, selector, String.valueOf(timeoutSeconds),
            "Wait for " + selector, true, null, null, Instant.now(), null);
    }

    public static PageAction screenshot(byte[] data) {
        return new PageAction(ActionType.SCREENSHOT, null, null, "Take screenshot",
            true, null, data, Instant.now(), null);
    }

    public static PageAction failed(ActionType type, String selector, String error) {
        return new PageAction(type, selector, null, "Failed: " + error,
            false, error, null, Instant.now(), null);
    }
}
