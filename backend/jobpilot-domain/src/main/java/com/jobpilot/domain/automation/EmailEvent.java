package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class EmailEvent extends BaseAggregateRoot {

    private EmailEventId eventId;
    private UUID userId;
    private UUID missionId;
    private String jobUrl;
    private String jobTitle;
    private String companyName;
    private EmailEventType eventType;
    private String senderEmail;
    private String subject;
    private String bodySnippet;
    private Instant receivedAt;
    private final Instant createdAt;

    private EmailEvent(EmailEventId eventId, UUID userId) {
        super(eventId.value());
        this.eventId = eventId;
        this.userId = userId;
        this.receivedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public static EmailEvent create(UUID userId, EmailEventType eventType, String senderEmail,
                                     String subject, String bodySnippet) {
        var event = new EmailEvent(EmailEventId.generate(), userId);
        event.eventType = eventType;
        event.senderEmail = senderEmail;
        event.subject = subject;
        event.bodySnippet = bodySnippet;
        return event;
    }

    public void linkToApplication(String jobUrl, String jobTitle, String companyName, UUID missionId) {
        this.jobUrl = jobUrl;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.missionId = missionId;
    }

    public EmailEventId eventId() { return eventId; }
    public UUID userId() { return userId; }
    public UUID missionId() { return missionId; }
    public String jobUrl() { return jobUrl; }
    public String jobTitle() { return jobTitle; }
    public String companyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public EmailEventType eventType() { return eventType; }
    public String senderEmail() { return senderEmail; }
    public String subject() { return subject; }
    public String bodySnippet() { return bodySnippet; }
    public Instant receivedAt() { return receivedAt; }
    public Instant createdAt() { return createdAt; }

    public enum EmailEventType {
        APPLICATION_CONFIRMATION,
        APPLICATION_REJECTION,
        INTERVIEW_INVITATION,
        OFFER_RECEIVED,
        UNKNOWN
    }

    public static class EmailEventId extends com.jobpilot.domain.shared.BaseValueObject {
        private final UUID value;

        private EmailEventId(UUID value) {
            this.value = value;
        }

        public static EmailEventId generate() {
            return new EmailEventId(UUID.randomUUID());
        }

        public static EmailEventId from(UUID uuid) {
            return new EmailEventId(uuid);
        }

        public UUID value() { return value; }

        @Override
        protected Object[] equalityFields() {
            return new Object[]{value};
        }
    }
}
