package com.jobpilot.domain.resume;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.resume.events.ResumeCreatedEvent;
import com.jobpilot.domain.resume.events.ResumeDeletedEvent;
import com.jobpilot.domain.resume.events.ResumeUpdatedEvent;
import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Resume extends BaseAggregateRoot {

    private ResumeId resumeId;
    private UserId userId;
    private String title;
    private Integer atsScore;
    private Map<String, Object> atsScoreData;
    private int resumeVersion;
    private boolean isDefault;
    private boolean deleted;
    private Instant deletedAt;
    private final List<ResumeSection> sections;
    private final Instant createdAt;
    private Instant updatedAt;

    private Resume(ResumeId resumeId, UserId userId, String title) {
        super(resumeId.value());
        this.resumeId = resumeId;
        this.userId = userId;
        this.title = title;
        this.resumeVersion = 1;
        this.isDefault = false;
        this.deleted = false;
        this.sections = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Resume create(ResumeId resumeId, UserId userId, String title) {
        var resume = new Resume(resumeId, userId, title);
        resume.registerEvent(new ResumeCreatedEvent(resumeId, userId));
        return resume;
    }

    public static Resume reconstitute(ResumeId resumeId, UserId userId, String title,
                                       Integer atsScore, Map<String, Object> atsScoreData,
                                       int resumeVersion, boolean isDefault, boolean deleted,
                                       Instant deletedAt, List<ResumeSection> sections,
                                       Instant createdAt, Instant updatedAt) {
        var resume = new Resume(resumeId, userId, title);
        resume.atsScore = atsScore;
        resume.atsScoreData = atsScoreData;
        resume.resumeVersion = resumeVersion;
        resume.isDefault = isDefault;
        resume.deleted = deleted;
        resume.deletedAt = deletedAt;
        resume.sections.addAll(sections);
        return resume;
    }

    public void updateTitle(String newTitle) {
        this.title = newTitle;
        this.resumeVersion++;
        this.updatedAt = Instant.now();
        registerEvent(new ResumeUpdatedEvent(resumeId, userId));
    }

    public ResumeSection addSection(ResumeSectionType type, String title, Map<String, Object> content, int sortOrder) {
        var section = new ResumeSection(type, title, content, sortOrder);
        sections.add(section);
        this.resumeVersion++;
        this.updatedAt = Instant.now();
        registerEvent(new ResumeUpdatedEvent(resumeId, userId));
        return section;
    }

    public void updateSection(int index, ResumeSectionType type, String title, Map<String, Object> content, int sortOrder) {
        if (index < 0 || index >= sections.size()) {
            throw new IllegalArgumentException("Section index out of bounds");
        }
        sections.get(index).update(type, title, content, sortOrder);
        this.resumeVersion++;
        this.updatedAt = Instant.now();
        registerEvent(new ResumeUpdatedEvent(resumeId, userId));
    }

    public void removeSection(int index) {
        if (index < 0 || index >= sections.size()) {
            throw new IllegalArgumentException("Section index out of bounds");
        }
        sections.remove(index);
        this.resumeVersion++;
        this.updatedAt = Instant.now();
        registerEvent(new ResumeUpdatedEvent(resumeId, userId));
    }

    public void reorderSections(List<Integer> newOrder) {
        if (newOrder.size() != sections.size()) {
            throw new IllegalArgumentException("New order must contain all section indices");
        }
        var reordered = new ArrayList<ResumeSection>();
        for (int idx : newOrder) {
            if (idx < 0 || idx >= sections.size()) {
                throw new IllegalArgumentException("Invalid section index: " + idx);
            }
            reordered.add(sections.get(idx));
        }
        sections.clear();
        sections.addAll(reordered);
        for (int i = 0; i < sections.size(); i++) {
            var s = sections.get(i);
            var sortOrder = i;
            s.update(s.type(), s.title(), s.content(), sortOrder);
        }
        this.resumeVersion++;
        this.updatedAt = Instant.now();
        registerEvent(new ResumeUpdatedEvent(resumeId, userId));
    }

    public void markDefault() {
        this.isDefault = true;
        this.updatedAt = Instant.now();
    }

    public void unmarkDefault() {
        this.isDefault = false;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (deleted) return;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
        registerEvent(new ResumeDeletedEvent(resumeId, userId));
    }

    public void updateAtsScore(int score, Map<String, Object> scoreData) {
        this.atsScore = score;
        this.atsScoreData = scoreData;
        this.updatedAt = Instant.now();
    }

    public ResumeId resumeId() { return resumeId; }
    public UserId userId() { return userId; }
    public String title() { return title; }
    public Integer atsScore() { return atsScore; }
    public Map<String, Object> atsScoreData() { return atsScoreData; }
    public int resumeVersion() { return resumeVersion; }
    public boolean isDefault() { return isDefault; }
    public boolean isDeleted() { return deleted; }
    public Instant deletedAt() { return deletedAt; }
    public List<ResumeSection> sections() { return List.copyOf(sections); }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
