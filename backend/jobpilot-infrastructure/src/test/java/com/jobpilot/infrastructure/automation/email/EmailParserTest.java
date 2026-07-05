package com.jobpilot.infrastructure.automation.email;

import com.jobpilot.domain.automation.EmailEvent.EmailEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailParserTest {

    private EmailParser parser;

    @BeforeEach
    void setUp() {
        parser = new EmailParser();
    }

    @Test
    @DisplayName("Should detect application confirmation")
    void shouldDetectConfirmation() {
        var type = parser.classifyEmail(
            "Application Received",
            "Thank you for your application. We have received your resume."
        );
        assertEquals(EmailEventType.APPLICATION_CONFIRMATION, type);
    }

    @Test
    @DisplayName("Should detect rejection")
    void shouldDetectRejection() {
        var type = parser.classifyEmail(
            "Update on your application",
            "Unfortunately we will not be moving forward with your application."
        );
        assertEquals(EmailEventType.APPLICATION_REJECTION, type);
    }

    @Test
    @DisplayName("Should detect interview invitation")
    void shouldDetectInterview() {
        var type = parser.classifyEmail(
            "Interview Invitation",
            "We would like to invite you for an interview for the Java Developer position."
        );
        assertEquals(EmailEventType.INTERVIEW_INVITATION, type);
    }

    @Test
    @DisplayName("Should detect job offer")
    void shouldDetectOffer() {
        var type = parser.classifyEmail(
            "Job Offer",
            "We are pleased to extend an offer of employment for the Senior Developer role."
        );
        assertEquals(EmailEventType.OFFER_RECEIVED, type);
    }

    @Test
    @DisplayName("Should return unknown for non-matching email")
    void shouldReturnUnknownForNonMatching() {
        var type = parser.classifyEmail(
            "Meeting tomorrow",
            "Let's schedule a meeting for tomorrow at 3pm."
        );
        assertEquals(EmailEventType.UNKNOWN, type);
    }

    @Test
    @DisplayName("Should identify job-related emails")
    void shouldIdentifyJobRelated() {
        assertTrue(parser.isJobRelated("Application Status", "Your application for Java Developer"));
        assertTrue(parser.isJobRelated("Interview", "We'd like to schedule an interview"));
        assertFalse(parser.isJobRelated("Lunch plans", "Want to grab lunch?"));
    }

    @Test
    @DisplayName("Should extract company name from email domain")
    void shouldExtractCompanyName() {
        assertEquals("LinkedIn", parser.extractCompanyName("jobs@linkedin.com"));
        assertEquals("Indeed", parser.extractCompanyName("noreply@indeed.com"));
        assertEquals("google", parser.extractCompanyName("careers@google.com"));
        assertEquals("Unknown", parser.extractCompanyName(""));
    }

    @Test
    @DisplayName("Should handle null sender email")
    void shouldHandleNullSender() {
        assertEquals("Unknown", parser.extractCompanyName(null));
    }
}
