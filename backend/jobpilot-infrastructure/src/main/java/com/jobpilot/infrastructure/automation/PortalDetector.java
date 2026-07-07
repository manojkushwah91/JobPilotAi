package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.PortalDetectorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Map.entry;

@Component
public class PortalDetector implements PortalDetectorPort {

    private static final Logger log = LoggerFactory.getLogger(PortalDetector.class);

    private static final Map<String, Pattern> PORTAL_PATTERNS = Map.ofEntries(
        entry("greenhouse", Pattern.compile("boards\\.greenhouse\\.io|greenhouse\\.com", Pattern.CASE_INSENSITIVE)),
        entry("lever", Pattern.compile("jobs\\.lever\\.co|lever\\.co", Pattern.CASE_INSENSITIVE)),
        entry("workday", Pattern.compile("myworkdayjobs\\.com|workday\\.com", Pattern.CASE_INSENSITIVE)),
        entry("icims", Pattern.compile("icims\\.com", Pattern.CASE_INSENSITIVE)),
        entry("taleo", Pattern.compile("taleo\\.net|oracle\\.com/taleo", Pattern.CASE_INSENSITIVE)),
        entry("smartrecruiters", Pattern.compile("smartrecruiters\\.com", Pattern.CASE_INSENSITIVE)),
        entry("ashby", Pattern.compile("ashbyhq\\.com|jobs\\.ashbyhq\\.com", Pattern.CASE_INSENSITIVE)),
        entry("bamboohr", Pattern.compile("bamboohr\\.com", Pattern.CASE_INSENSITIVE)),
        entry("jazz", Pattern.compile("jazz\\.co|jazzhr\\.com", Pattern.CASE_INSENSITIVE)),
        entry("successfactors", Pattern.compile("successfactors\\.com|sap\\.com", Pattern.CASE_INSENSITIVE)),
        entry("jobvite", Pattern.compile("jobvite\\.com", Pattern.CASE_INSENSITIVE)),
        entry("paycom", Pattern.compile("paycomonline\\.net", Pattern.CASE_INSENSITIVE)),
        entry("ultipro", Pattern.compile("ultipro\\.com|paycom\\.com", Pattern.CASE_INSENSITIVE)),
        entry("adp", Pattern.compile("adp\\.com", Pattern.CASE_INSENSITIVE)),
        entry("greenhouse-form", Pattern.compile("boards\\.greenhouse\\.io/.*/applications", Pattern.CASE_INSENSITIVE)),
        entry("lever-form", Pattern.compile("jobs\\.lever\\.co/.*/apply", Pattern.CASE_INSENSITIVE))
    );

    private static final Map<String, Map<String, String>> PORTAL_SELECTORS = Map.of(
        "greenhouse", Map.of(
            "firstName", "#first_name",
            "lastName", "#last_name",
            "email", "#email",
            "phone", "#phone",
            "resume", "input[type='file']",
            "submit", "input[type='submit'], button[type='submit']",
            "coverLetter", "#cover_letter_text"
        ),
        "lever", Map.of(
            "name", "input[name='name']",
            "email", "input[name='email']",
            "phone", "input[name='phone']",
            "resume", "input[type='file']",
            "submit", "button[data-qa='btn-submit']",
            "urls", "input[name='urls']"
        ),
        "workday", Map.of(
            "firstName", "input[data-automation-id='legalNameSection_firstName']",
            "lastName", "input[data-automation-id='legalNameSection_lastName']",
            "email", "input[data-automation-id='email']",
            "phone", "input[data-automation-id='phone-number']",
            "resume", "input[data-automation-id='file-upload-input-ref']",
            "submit", "button[data-automation-id='bottom-navigation-next-button']"
        ),
        "ashby", Map.of(
            "name", "input[name='name']",
            "email", "input[name='email']",
            "phone", "input[name='phone']",
            "resume", "input[type='file']",
            "submit", "button[type='submit']"
        ),
        "smartrecruiters", Map.of(
            "firstName", "input[data-test='candidateFirstNam input']",
            "lastName", "input[data-test='candidateLastName']",
            "email", "input[data-test='candidateEmail']",
            "phone", "input[data-test='candidatePhone']",
            "resume", "input[data-test='file-upload-input']",
            "submit", "button[data-test='submit-application']"
        )
    );

    @Override
    public String detectPortal(String url) {
        if (url == null || url.isBlank()) return "unknown";

        for (var entry : PORTAL_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(url).find()) {
                log.info("Detected portal: {} for URL: {}", entry.getKey(), url);
                return entry.getKey();
            }
        }
        return "unknown";
    }

    @Override
    public Map<String, String> getPortalSelectors(String portal) {
        return PORTAL_SELECTORS.getOrDefault(portal, Map.of());
    }

    @Override
    public boolean isApplicationForm(String url) {
        if (url == null) return false;
        return url.contains("/apply") || url.contains("/applications")
            || url.contains("/submit") || url.contains("/easyapply");
    }
}
