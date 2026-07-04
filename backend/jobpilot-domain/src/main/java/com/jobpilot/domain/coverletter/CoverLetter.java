package com.jobpilot.domain.coverletter;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;

public class CoverLetter extends BaseAggregateRoot {

    private CoverLetterId coverLetterId;
    private UserId userId;
    private String title;
    private String companyName;
    private String jobTitle;
    private String content;
    private String tone;
    private String recipientName;
    private boolean aiGenerated;
    private boolean deleted;
    private Instant deletedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private CoverLetter(CoverLetterId coverLetterId, UserId userId, String title) {
        super(coverLetterId.value());
        this.coverLetterId = coverLetterId;
        this.userId = userId;
        this.title = title;
        this.tone = "PROFESSIONAL";
        this.aiGenerated = false;
        this.deleted = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static CoverLetter create(CoverLetterId coverLetterId, UserId userId, String title) {
        return new CoverLetter(coverLetterId, userId, title);
    }

    public static CoverLetter reconstitute(CoverLetterId coverLetterId, UserId userId,
                                            String title, String companyName, String jobTitle, String content,
                                            String tone, String recipientName, boolean aiGenerated,
                                            boolean deleted, Instant deletedAt,
                                            Instant createdAt, Instant updatedAt) {
        var cl = new CoverLetter(coverLetterId, userId, title);
        cl.companyName = companyName;
        cl.jobTitle = jobTitle;
        cl.content = content;
        cl.tone = tone;
        cl.recipientName = recipientName;
        cl.aiGenerated = aiGenerated;
        cl.deleted = deleted;
        cl.deletedAt = deletedAt;
        return cl;
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void updateCompanyName(String companyName) {
        this.companyName = companyName;
        this.updatedAt = Instant.now();
    }

    public void updateTone(String tone) {
        this.tone = tone;
        this.updatedAt = Instant.now();
    }

    public void updateRecipientName(String recipientName) {
        this.recipientName = recipientName;
        this.updatedAt = Instant.now();
    }

    public void markAiGenerated() {
        this.aiGenerated = true;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (deleted) return;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public CoverLetterId coverLetterId() { return coverLetterId; }
    public UserId userId() { return userId; }
    public String title() { return title; }
    public String companyName() { return companyName; }
    public String jobTitle() { return jobTitle; }
    public String content() { return content; }
    public String tone() { return tone; }
    public String recipientName() { return recipientName; }
    public boolean isAiGenerated() { return aiGenerated; }
    public boolean isDeleted() { return deleted; }
    public Instant deletedAt() { return deletedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
