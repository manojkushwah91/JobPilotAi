package com.jobpilot.application.automation.ports;

import java.util.List;

public interface ScreenshotManagerPort {

    byte[] takeScreenshot();

    byte[] takeElementScreenshot(String selector);

    byte[] takeFullPageScreenshot();

    String saveScreenshot(byte[] data, String name);

    List<String> getScreenshotHistory(String sessionId);

    default byte[] takeScreenshotOnError(String errorContext) {
        return takeScreenshot();
    }
}
