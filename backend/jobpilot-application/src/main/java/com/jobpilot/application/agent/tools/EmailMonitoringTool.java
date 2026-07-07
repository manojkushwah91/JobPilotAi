package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.NotificationPort;
import com.jobpilot.application.notification.ports.EmailMonitorPort;
import com.jobpilot.application.notification.ports.EmailParserPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EmailMonitoringTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(EmailMonitoringTool.class);

    @Autowired(required = false)
    private EmailMonitorPort emailMonitor;

    @Autowired(required = false)
    private EmailParserPort emailParser;

    @Autowired(required = false)
    private NotificationPort notificationPort;

    @Override
    public String name() {
        return "MONITOR_EMAILS";
    }

    @Override
    public String description() {
        return "Monitors email for job application responses (interviews, rejections, offers)";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing email monitoring tool");

        if (emailMonitor == null || !emailMonitor.isAuthenticated()) {
            return Map.of(
                "status", "error",
                "error", "Email monitoring not configured. Set jobpilot.email.imap.* properties."
            );
        }

        var userId = input.get("userId") instanceof UUID uid ? uid : null;
        var maxEmails = input.containsKey("maxEmails") ? ((Number) input.get("maxEmails")).intValue() : 20;

        try {
            var emails = emailMonitor.fetchRecentEmails("INBOX", maxEmails);
            var signals = new ArrayList<Map<String, Object>>();
            var interviewCount = 0;
            var rejectionCount = 0;
            var offerCount = 0;

            for (var email : emails) {
                var subject = (String) email.get("subject");
                var body = (String) email.get("body");

                if (emailParser != null) {
                    var signal = emailParser.parseEmail(subject, body);
                    if (!"none".equals(signal.signalType())) {
                        var signalMap = new LinkedHashMap<String, Object>();
                        signalMap.put("type", signal.signalType());
                        signalMap.put("company", signal.company());
                        signalMap.put("jobTitle", signal.jobTitle());
                        signalMap.put("confidence", signal.confidence());
                        signalMap.put("keywords", signal.keywords());
                        signalMap.put("summary", signal.summary());
                        signalMap.put("emailSubject", subject);
                        signalMap.put("emailFrom", email.get("from"));
                        signalMap.put("emailDate", email.get("date"));
                        signals.add(signalMap);

                        switch (signal.signalType()) {
                            case "interview_invite" -> {
                                interviewCount++;
                                if (notificationPort != null && userId != null) {
                                    notificationPort.notifyUser(userId,
                                        "Interview Invitation",
                                        "You received an interview invitation from " + signal.company(),
                                        "agent");
                                }
                            }
                            case "rejection" -> rejectionCount++;
                            case "offer" -> {
                                offerCount++;
                                if (notificationPort != null && userId != null) {
                                    notificationPort.notifyUser(userId,
                                        "Job Offer Received",
                                        "You received a job offer from " + signal.company(),
                                        "agent");
                                }
                            }
                        }
                    }
                }
            }

            log.info("Email monitoring complete: {} signals ({} interviews, {} rejections, {} offers)",
                signals.size(), interviewCount, rejectionCount, offerCount);

            return Map.of(
                "status", "success",
                "totalEmailsScanned", emails.size(),
                "signalsFound", signals.size(),
                "interviews", interviewCount,
                "rejections", rejectionCount,
                "offers", offerCount,
                "signals", signals
            );

        } catch (Exception e) {
            log.error("Email monitoring failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @Override
    public int timeoutSeconds() {
        return 60;
    }
}
