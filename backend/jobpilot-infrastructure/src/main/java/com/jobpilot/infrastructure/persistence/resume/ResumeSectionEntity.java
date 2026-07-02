package com.jobpilot.infrastructure.persistence.resume;

import com.jobpilot.domain.resume.ResumeSection;
import com.jobpilot.domain.resume.ResumeSectionType;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "resume_sections")
public class ResumeSectionEntity extends BaseJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private ResumeEntity resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ResumeSectionType type;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private String content;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected ResumeSectionEntity() {}

    public static ResumeSectionEntity fromDomain(ResumeSection section, ResumeEntity resume) {
        var entity = new ResumeSectionEntity();
        entity.id = section.id();
        entity.resume = resume;
        entity.type = section.type();
        entity.title = section.title();
        entity.content = mapToJson(section.content());
        entity.sortOrder = section.sortOrder();
        return entity;
    }

    public ResumeSection toDomain() {
        var section = new ResumeSection(type, title, parseJson(content), sortOrder);
        return section;
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
    public ResumeSectionType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getSortOrder() { return sortOrder; }
}
