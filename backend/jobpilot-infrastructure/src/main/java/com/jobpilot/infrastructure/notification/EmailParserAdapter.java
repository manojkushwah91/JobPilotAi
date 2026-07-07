package com.jobpilot.infrastructure.notification;

import com.jobpilot.application.notification.ports.EmailParserPort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class EmailParserAdapter implements EmailParserPort {

    private static final Map<String, List<Pattern>> SIGNAL_PATTERNS = new LinkedHashMap<>();

    static {
        SIGNAL_PATTERNS.put("interview_invite", List.of(
            Pattern.compile("interview", Pattern.CASE_INSENSITIVE),
            Pattern.compile("schedule.*(?:call|meeting|chat)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:next|next step).*process", Pattern.CASE_INSENSITIVE),
            Pattern.compile("we(?:'d| would) like to.*(?:speak|talk|meet)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("move forward.*(?:process|interview)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("selected.*(?:interview|phone screen)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("invite.*(?:interview|call)", Pattern.CASE_INSENSITIVE)
        ));

        SIGNAL_PATTERNS.put("rejection", List.of(
            Pattern.compile("unfortunately.*(?:not|position)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("we have decided to.*(?:move on|go with another)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("not.*(?:selected|chosen|proceed)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("position.*(?:filled|closed)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("after careful consideration", Pattern.CASE_INSENSITIVE),
            Pattern.compile("regret.*(?:inform|notify)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("not.*(?:a fit|the right fit)", Pattern.CASE_INSENSITIVE)
        ));

        SIGNAL_PATTERNS.put("offer", List.of(
            Pattern.compile("offer", Pattern.CASE_INSENSITIVE),
            Pattern.compile("congratulations", Pattern.CASE_INSENSITIVE),
            Pattern.compile("pleased to.*(?:offer|inform)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("position.*(?:offered|available)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("compensation.*package", Pattern.CASE_INSENSITIVE),
            Pattern.compile("start date", Pattern.CASE_INSENSITIVE)
        ));

        SIGNAL_PATTERNS.put("screening", List.of(
            Pattern.compile("screening", Pattern.CASE_INSENSITIVE),
            Pattern.compile("phone screen", Pattern.CASE_INSENSITIVE),
            Pattern.compile("initial.*(?:call|conversation)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("recruiter.*(?:call|reach out)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("quick chat", Pattern.CASE_INSENSITIVE)
        ));

        SIGNAL_PATTERNS.put("follow_up", List.of(
            Pattern.compile("follow.?up", Pattern.CASE_INSENSITIVE),
            Pattern.compile("checking in", Pattern.CASE_INSENSITIVE),
            Pattern.compile("status.*(?:update|check)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("any.*(?:update|news)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("still.*(?:interested|considering)", Pattern.CASE_INSENSITIVE)
        ));
    }

    private static final Pattern COMPANY_PATTERN = Pattern.compile(
        "(?:from|at|@)\\s+([A-Z][A-Za-z0-9\\s&.]+?)(?:\\s|,|\\.|$)", Pattern.MULTILINE
    );

    @Override
    public ParsedEmailSignal parseEmail(String subject, String body) {
        var text = ((subject != null ? subject : "") + " " + (body != null ? body : "")).toLowerCase();

        var bestSignal = "none";
        var bestScore = 0.0;
        var matchedKeywords = new ArrayList<String>();

        for (var entry : SIGNAL_PATTERNS.entrySet()) {
            var signalScore = 0.0;
            var signalKeywords = new ArrayList<String>();

            for (var pattern : entry.getValue()) {
                var matcher = pattern.matcher(text);
                if (matcher.find()) {
                    signalScore += 1.0;
                    signalKeywords.add(matcher.group());
                }
            }

            if (signalScore > bestScore) {
                bestScore = signalScore;
                bestSignal = entry.getKey();
                matchedKeywords = signalKeywords;
            }
        }

        var company = extractCompany(subject + " " + (body != null ? body : ""));
        var jobTitle = extractJobTitle(subject);

        var confidence = Math.min(bestScore / 3.0, 1.0);
        if (bestScore == 0) confidence = 0;

        var summary = switch (bestSignal) {
            case "interview_invite" -> "Interview invitation detected";
            case "rejection" -> "Application rejection detected";
            case "offer" -> "Job offer detected";
            case "screening" -> "Screening call request detected";
            case "follow_up" -> "Follow-up email detected";
            default -> "No relevant signal detected";
        };

        return new ParsedEmailSignal(bestSignal, company, jobTitle, confidence, matchedKeywords, summary);
    }

    private String extractCompany(String text) {
        var matcher = COMPANY_PATTERN.matcher(text);
        if (matcher.find()) {
            var company = matcher.group(1).trim();
            if (company.length() > 3 && company.length() < 60) {
                return company;
            }
        }
        return null;
    }

    private String extractJobTitle(String subject) {
        if (subject == null) return null;
        var titlePattern = Pattern.compile(
            "(?:re:|regarding|application for|position:?)\\s*(.+?)(?:\\s*-|\\s*\\|)", Pattern.CASE_INSENSITIVE
        );
        var matcher = titlePattern.matcher(subject);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
