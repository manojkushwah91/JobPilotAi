package com.jobpilot.infrastructure.persistence.resume;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.resume.Resume;
import com.jobpilot.domain.resume.ResumeId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "resumes")
public class ResumeEntity extends BaseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "ats_score")
    private Integer atsScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ats_score_data", columnDefinition = "jsonb")
    private String atsScoreData;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<ResumeSectionEntity> sections = new ArrayList<>();

    protected ResumeEntity() {}

    public static ResumeEntity fromDomain(Resume resume) {
        var entity = new ResumeEntity();
        entity.id = resume.resumeId().value();
        entity.userId = resume.userId().value();
        entity.title = resume.title();
        entity.atsScore = resume.atsScore();
        entity.atsScoreData = resume.atsScoreData() != null ? mapToJson(resume.atsScoreData()) : null;
        entity.version = resume.resumeVersion();
        entity.isDefault = resume.isDefault();
        entity.deletedAt = resume.deletedAt();
        entity.sections = resume.sections().stream()
            .map(s -> ResumeSectionEntity.fromDomain(s, entity))
            .toList();
        return entity;
    }

    public Resume toDomain() {
        var resumeId = ResumeId.from(id);
        var userIdVo = UserId.from(userId);
        var domainSections = sections.stream()
            .map(ResumeSectionEntity::toDomain)
            .toList();

        return Resume.reconstitute(
            resumeId, userIdVo, title, atsScore,
            atsScoreData != null ? parseJson(atsScoreData) : null,
            version, isDefault, deletedAt != null, deletedAt,
            domainSections, createdAt, updatedAt
        );
    }

    private static String mapToJson(Map<String, Object> map) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public Integer getAtsScore() { return atsScore; }
    public String getAtsScoreData() { return atsScoreData; }
    public int getVersion() { return version; }
    public boolean isDefault() { return isDefault; }
    public Instant getDeletedAt() { return deletedAt; }
    public List<ResumeSectionEntity> getSections() { return sections; }
}
