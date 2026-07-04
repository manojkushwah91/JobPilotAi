package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.NotificationPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NotificationTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(NotificationTool.class);

    private final NotificationPort notificationPort;

    public NotificationTool(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    @Override
    public String name() {
        return "SEND_NOTIFICATION";
    }

    @Override
    public String description() {
        return "Sends a notification to the user";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Sending notification");

        var userId = UUID.fromString((String) input.getOrDefault("userId", ""));
        var title = (String) input.getOrDefault("title", "");
        var message = (String) input.getOrDefault("message", "");
        var channel = (String) input.getOrDefault("channel", "IN_APP");

        notificationPort.notifyUser(userId, title, message, channel);

        return Map.of(
            "status", "success",
            "channel", channel
        );
    }
}
