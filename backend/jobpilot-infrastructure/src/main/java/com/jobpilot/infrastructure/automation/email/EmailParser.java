package com.jobpilot.infrastructure.automation.email;

import com.jobpilot.domain.automation.EmailEvent.EmailEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class EmailParser {

    private static final Logger log = LoggerFactory.getLogger(EmailParser.class);

    private static final Map<EmailEventType, Pattern> CONFIRMATION_PATTERNS = Map.of(
        EmailEventType.APPLICATION_CONFIRMATION, Pattern.compile(
            "(?i)(thank you for (your )?application|application (has been )?received|" +
            "we(ve| have) received your (application|resume|cv)|application confirmed|" +
            "successfully submitted|application (was |has been )?submitted|" +
            "we(.ll| will) review your (application|resume)|application status.*submitted)",
            Pattern.CASE_INSENSITIVE
        ),
        EmailEventType.INTERVIEW_INVITATION, Pattern.compile(
            "(?i)(interview (invitation|invite|scheduled|request)|" +
            "we( like| would like) to (invite|schedule) you|interview.*next steps|" +
            "phone screen|technical interview|interview.*position)",
            Pattern.CASE_INSENSITIVE
        ),
        EmailEventType.OFFER_RECEIVED, Pattern.compile(
            "(?i)(job offer|offer letter|we( are|re) pleased to (offer|extend)|" +
            "congratulations.*selected|offer of employment|compensation package|" +
            "welcome to.*team)",
            Pattern.CASE_INSENSITIVE
        ),
        EmailEventType.APPLICATION_REJECTION, Pattern.compile(
            "(?i)(unfortunately.*not .*mov|unfortunately.*not .*proceed|" +
            "not (selected|chosen|moving forward)|" +
            "position (has been )?(filled|closed)|other candidates|" +
            "regret to (inform|inform you)|decided to (move|go) with another|" +
            "not (a |the )?right (fit|match)|after careful review.*not proceed|" +
            "we won.t be (moving|proceeding))",
            Pattern.CASE_INSENSITIVE
        )
    );

    public EmailEventType classifyEmail(String subject, String body) {
        var text = (subject + " " + body).toLowerCase();

        for (var entry : CONFIRMATION_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(text).find()) {
                log.debug("Classified email as {} based on pattern match", entry.getKey());
                return entry.getKey();
            }
        }

        return EmailEventType.UNKNOWN;
    }

    public boolean isJobRelated(String subject, String body) {
        var text = (subject + " " + body).toLowerCase();
        return text.contains("application") ||
               text.contains("resume") ||
               text.contains("position") ||
               text.contains("job") ||
               text.contains("career") ||
               text.contains("interview") ||
               text.contains("hiring") ||
               text.contains("offer");
    }

    public String extractCompanyName(String senderEmail) {
        if (senderEmail == null || senderEmail.isEmpty()) return "Unknown";

        var domain = senderEmail.substring(senderEmail.indexOf('@') + 1);
        domain = domain.toLowerCase();

        var knownDomains = Map.of(
            "linkedin.com", "LinkedIn",
            "indeed.com", "Indeed",
            "greenhouse.io", "Greenhouse",
            "lever.co", "Lever",
            "workday.com", "Workday",
            "icims.com", "iCIMS",
            "smartrecruiters.com", "SmartRecruiters",
            "jazz.co", "JazzHR",
            "bamboohr.com", "BambooHR"
        );

        for (var entry : knownDomains.entrySet()) {
            if (domain.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return domain.split("\\.")[0];
    }
}
