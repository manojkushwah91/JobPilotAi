package com.jobpilot.application.notification.ports;

import java.util.List;
import java.util.Map;

public interface EmailParserPort {

    ParsedEmailSignal parseEmail(String subject, String body);

    record ParsedEmailSignal(
        String signalType,
        String company,
        String jobTitle,
        double confidence,
        List<String> keywords,
        String summary
    ) {
        public static ParsedEmailSignal none() {
            return new ParsedEmailSignal("none", null, null, 0.0, List.of(), "No relevant signal detected");
        }
    }
}
