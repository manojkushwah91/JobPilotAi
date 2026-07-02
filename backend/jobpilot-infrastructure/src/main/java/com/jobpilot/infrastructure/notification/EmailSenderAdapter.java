package com.jobpilot.infrastructure.notification;

import com.jobpilot.application.notification.ports.EmailSenderPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderAdapter.class);

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
        if (mailSender == null) {
            log.info("[EMAIL] To: {}, Template: {}, Variables: {}", to, templateName, variables);
            return;
        }
        log.info("[EMAIL] Template {} not yet resolved; sending to {} with variables {}", templateName, to, variables);
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
