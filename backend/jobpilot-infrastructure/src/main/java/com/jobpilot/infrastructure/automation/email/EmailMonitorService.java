package com.jobpilot.infrastructure.automation.email;

import com.jobpilot.domain.automation.EmailEvent;
import com.jobpilot.infrastructure.automation.progress.AutomationProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailMonitorService {

    private static final Logger log = LoggerFactory.getLogger(EmailMonitorService.class);

    private final EmailParser emailParser;
    private final AutomationProgressTracker progressTracker;
    private final Map<String, EmailEvent> processedEmails = new ConcurrentHashMap<>();
    private final List<EmailEvent> recentEvents = Collections.synchronizedList(new ArrayList<>());

    public EmailMonitorService(EmailParser emailParser, AutomationProgressTracker progressTracker) {
        this.emailParser = emailParser;
        this.progressTracker = progressTracker;
    }

    public EmailEvent processIncomingEmail(String messageId, String senderEmail,
                                            String subject, String body, UUID userId) {
        if (processedEmails.containsKey(messageId)) {
            log.debug("Already processed email: {}", messageId);
            return processedEmails.get(messageId);
        }

        if (!emailParser.isJobRelated(subject, body)) {
            log.debug("Email not job-related, skipping: {}", subject);
            return null;
        }

        var eventType = emailParser.classifyEmail(subject, body);
        var companyName = emailParser.extractCompanyName(senderEmail);

        var event = EmailEvent.create(userId, eventType, senderEmail, subject,
            body.length() > 500 ? body.substring(0, 500) : body);
        event.setCompanyName(companyName);

        processedEmails.put(messageId, event);
        recentEvents.add(0, event);

        if (recentEvents.size() > 100) {
            recentEvents.subList(100, recentEvents.size()).clear();
        }

        log.info("Processed email event: {} from {} ({})", eventType, senderEmail, companyName);

        return event;
    }

    public EmailEvent processEmail(String messageId, String senderEmail,
                                    String subject, String body, UUID userId,
                                    String jobUrl, String jobTitle, UUID missionId) {
        var event = processIncomingEmail(messageId, senderEmail, subject, body, userId);
        if (event != null && jobUrl != null) {
            event.linkToApplication(jobUrl, jobTitle, emailParser.extractCompanyName(senderEmail), missionId);
        }
        return event;
    }

    public List<EmailEvent> getRecentEvents() {
        return List.copyOf(recentEvents);
    }

    public List<EmailEvent> getEventsByType(EmailEvent.EmailEventType type) {
        return recentEvents.stream()
            .filter(e -> e.eventType() == type)
            .toList();
    }

    public List<EmailEvent> getEventsByUser(UUID userId) {
        return recentEvents.stream()
            .filter(e -> e.userId().equals(userId))
            .toList();
    }

    public Map<EmailEvent.EmailEventType, Long> getEventCounts() {
        return recentEvents.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                EmailEvent::eventType,
                java.util.stream.Collectors.counting()
            ));
    }

    public boolean hasProcessed(String messageId) {
        return processedEmails.containsKey(messageId);
    }

    public int getProcessedCount() {
        return processedEmails.size();
    }
}
