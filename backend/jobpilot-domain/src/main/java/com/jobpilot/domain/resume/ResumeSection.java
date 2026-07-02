package com.jobpilot.domain.resume;

import com.jobpilot.domain.shared.BaseEntity;

import java.util.Map;

public class ResumeSection extends BaseEntity {

    private ResumeSectionType type;
    private String title;
    private Map<String, Object> content;
    private int sortOrder;

    protected ResumeSection() {}

    public ResumeSection(ResumeSectionType type, String title, Map<String, Object> content, int sortOrder) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder;
    }

    public void update(ResumeSectionType type, String title, Map<String, Object> content, int sortOrder) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder;
        incrementVersion();
    }

    public ResumeSectionType type() { return type; }
    public String title() { return title; }
    public Map<String, Object> content() { return content; }
    public int sortOrder() { return sortOrder; }
}
