package com.jobpilot.domain.application;

import com.jobpilot.domain.shared.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public class ApplicationNote extends BaseEntity {

    private final ApplicationNoteId noteId;
    private UUID applicationId;
    private UUID userId;
    private String content;
    private String category;
    private Instant createdAt;
    private Instant updatedAt;

    public ApplicationNote(ApplicationNoteId noteId, UUID applicationId, UUID userId, String content, String category) {
        this.noteId = noteId;
        this.applicationId = applicationId;
        this.userId = userId;
        this.content = content;
        this.category = category != null ? category : "GENERAL";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public ApplicationNoteId noteId() { return noteId; }
    public UUID applicationId() { return applicationId; }
    public UUID userId() { return userId; }
    public String content() { return content; }
    public String category() { return category; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    public void updateContent(String content) { this.content = content; this.updatedAt = Instant.now(); }
}
