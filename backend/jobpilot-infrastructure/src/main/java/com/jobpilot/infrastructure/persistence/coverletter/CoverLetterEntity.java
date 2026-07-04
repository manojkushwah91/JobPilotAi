package com.jobpilot.infrastructure.persistence.coverletter;

import com.jobpilot.domain.coverletter.CoverLetter;
import com.jobpilot.domain.coverletter.CoverLetterId;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cover_letters")
public class CoverLetterEntity extends BaseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "recipient_name")
    private String recipientName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "body", columnDefinition = "jsonb", nullable = false)
    private String body;

    @Column(name = "tone", nullable = false)
    private String tone;

    @Column(name = "word_count", nullable = false)
    private int wordCount;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected CoverLetterEntity() {}

    public static CoverLetterEntity fromDomain(CoverLetter coverLetter) {
        var entity = new CoverLetterEntity();
        entity.id = coverLetter.coverLetterId().value();
        entity.userId = coverLetter.userId().value();
        entity.title = coverLetter.title();
        entity.companyName = coverLetter.companyName();
        entity.jobTitle = coverLetter.jobTitle();
        entity.recipientName = coverLetter.recipientName();
        entity.body = "\"" + escapeJson(coverLetter.content()) + "\"";
        entity.tone = coverLetter.tone();
        entity.wordCount = coverLetter.content() != null ? coverLetter.content().split("\\s+").length : 0;
        entity.version = 1;
        entity.deletedAt = coverLetter.deletedAt();
        return entity;
    }

    public CoverLetter toDomain() {
        return CoverLetter.reconstitute(
            CoverLetterId.from(id),
            UserId.from(userId),
            title,
            companyName,
            jobTitle,
            unescapeJson(body),
            tone,
            recipientName,
            false,
            deletedAt != null,
            deletedAt,
            createdAt,
            updatedAt
        );
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescapeJson(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getCompanyName() { return companyName; }
    public String getRecipientName() { return recipientName; }
    public String getBody() { return body; }
    public String getTone() { return tone; }
    public int getWordCount() { return wordCount; }
    public int getVersion() { return version; }
    public String getFileUrl() { return fileUrl; }
    public Instant getDeletedAt() { return deletedAt; }
}
