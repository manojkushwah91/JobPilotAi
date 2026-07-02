package com.jobpilot.application.notification.ports;

import java.util.Map;

public interface EmailSenderPort {
    void send(String to, String subject, String htmlBody);
    void sendWithTemplate(String to, String templateName, Map<String, Object> variables);
}
