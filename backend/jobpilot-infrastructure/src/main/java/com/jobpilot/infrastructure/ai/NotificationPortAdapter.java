package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.agent.ports.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationPortAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationPortAdapter.class);

    @Override
    public void notifyUser(UUID userId, String title, String message, String channel) {
        log.info("[Notification] User={}, Title='{}', Channel={}: {}", userId, title, channel, message);
    }

    @Override
    public void notifyMissionUpdate(UUID userId, String missionId, String status, String details) {
        log.info("[MissionUpdate] User={}, Mission={}, Status={}: {}", userId, missionId, status, details);
    }

    @Override
    public void notifyTaskComplete(UUID userId, String taskId, String taskType, String result) {
        log.info("[TaskComplete] User={}, Task={}, Type={}: {}", userId, taskId, taskType, result);
    }

    @Override
    public void notifyError(UUID userId, String taskId, String error) {
        log.error("[Error] User={}, Task={}: {}", userId, taskId, error);
    }

    @Override
    public void notifyApprovalRequired(UUID userId, String taskId, String details) {
        log.warn("[ApprovalRequired] User={}, Task={}: {}", userId, taskId, details);
    }
}
