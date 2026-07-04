package com.jobpilot.application.agent.ports;

import java.util.UUID;

public interface NotificationPort {

    void notifyUser(UUID userId, String title, String message, String channel);

    void notifyMissionUpdate(UUID userId, String missionId, String status, String details);

    void notifyTaskComplete(UUID userId, String taskId, String taskType, String result);

    void notifyError(UUID userId, String taskId, String error);

    void notifyApprovalRequired(UUID userId, String taskId, String details);
}
