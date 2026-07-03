package com.jobpilot.infrastructure.notification;

import com.jobpilot.application.notification.ports.EmailSenderPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class EmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderAdapter.class);
    private static final String TEMPLATE_DIR = "templates/email/";
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String htmlBody) {
        if (mailSender == null) {
            log.info("[EMAIL] To: {}, Subject: {}, Body: {}...", to, subject, htmlBody.substring(0, Math.min(100, htmlBody.length())));
            return;
        }
        sendReal(to, subject, htmlBody);
    }

    @Override
    public void sendWithTemplate(String to, String templateName, Map<String, Object> variables) {
        var html = resolveTemplate(templateName, variables);
        if (mailSender == null) {
            log.info("[EMAIL] To: {}, Template: {}, Resolved body: {}...", to, templateName, html.substring(0, Math.min(100, html.length())));
            return;
        }
        var subject = resolveSubject(templateName, variables);
        sendReal(to, subject, html);
    }

    private String resolveTemplate(String templateName, Map<String, Object> variables) {
        var raw = templateCache.computeIfAbsent(templateName, this::loadTemplate);
        var result = raw;
        if (variables != null) {
            for (var entry : variables.entrySet()) {
                var val = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace("{{" + entry.getKey() + "}}", val);
            }
        }
        return result;
    }

    private String resolveSubject(String templateName, Map<String, Object> variables) {
        if ("welcome".equals(templateName)) return "Welcome to JobPilot AI!";
        if ("password-reset".equals(templateName)) return "Reset Your JobPilot Password";
        if ("application-status".equals(templateName)) return "Application Status Update";
        if ("interview-feedback".equals(templateName)) return "Interview Feedback Ready";
        if ("automation-completed".equals(templateName)) return "Automation Complete";
        if ("automation-failed".equals(templateName)) return "Automation Failed";
        return "Notification from JobPilot AI";
    }

    private String loadTemplate(String templateName) {
        try {
            var resource = new ClassPathResource(TEMPLATE_DIR + templateName + ".html");
            if (!resource.exists()) {
                log.warn("Email template {} not found, using fallback", templateName);
                return "<html><body><h1>{{title}}</h1><p>{{message}}</p></body></html>";
            }
            try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.warn("Failed to load email template {}: {}", templateName, e.getMessage());
            return "<html><body><h1>{{title}}</h1><p>{{message}}</p></body></html>";
        }
    }

    private void sendReal(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("noreply@jobpilot.ai");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
