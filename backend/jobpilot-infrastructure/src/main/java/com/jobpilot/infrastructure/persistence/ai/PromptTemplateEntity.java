package com.jobpilot.infrastructure.persistence.ai;

import com.jobpilot.domain.ai.PromptTemplate;
import com.jobpilot.domain.ai.PromptTemplateId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prompt_templates")
public class PromptTemplateEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "use_case", nullable = false) private String useCase;
    @Column(name = "name", nullable = false) private String name;
    @Column(name = "version", nullable = false) private int version;
    @Column(name = "system_prompt", nullable = false, columnDefinition = "text") private String systemPrompt;
    @Column(name = "user_prompt_template", nullable = false, columnDefinition = "text") private String userPromptTemplate;
    @Column(name = "variables", columnDefinition = "jsonb") private String variables;
    @Column(name = "model") private String model;
    @Column(name = "temperature") private double temperature;
    @Column(name = "max_tokens") private int maxTokens;
    @Column(name = "is_active") private boolean active;

    protected PromptTemplateEntity() {}

    public static PromptTemplateEntity fromDomain(PromptTemplate pt) {
        var e = new PromptTemplateEntity();
        e.id = pt.promptTemplateId().value();
        e.useCase = pt.useCase();
        e.name = pt.name();
        e.version = pt.templateVersion();
        e.systemPrompt = pt.systemPrompt();
        e.userPromptTemplate = pt.userPromptTemplate();
        e.variables = toJson(pt.variables());
        e.model = pt.model();
        e.temperature = pt.temperature();
        e.maxTokens = pt.maxTokens();
        e.active = pt.isActive();
        return e;
    }

    @SuppressWarnings("unchecked")
    public PromptTemplate toDomain() {
        return PromptTemplate.reconstitute(PromptTemplateId.from(id), useCase, name, version,
            systemPrompt, userPromptTemplate, fromJsonList(variables), model, temperature, maxTokens, active);
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : "[]"; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
    @SuppressWarnings("unchecked")
    private static List<String> fromJsonList(String json) {
        try { return (json != null && !json.isBlank()) ? MAPPER.readValue(json, List.class) : List.of(); }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
}
