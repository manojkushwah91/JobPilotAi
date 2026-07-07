package com.jobpilot.application.notification.ports;

import java.util.List;
import java.util.Map;

public interface EmailMonitorPort {

    List<Map<String, Object>> fetchRecentEmails(String folder, int maxCount);

    List<Map<String, Object>> searchEmails(String query, int maxCount);

    Map<String, Object> getEmail(String messageId);

    boolean isAuthenticated();

    default int pollingIntervalSeconds() {
        return 300;
    }
}
