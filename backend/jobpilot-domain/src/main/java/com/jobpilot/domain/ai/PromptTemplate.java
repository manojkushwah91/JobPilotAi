package com.jobpilot.domain.ai;

import com.jobpilot.domain.shared.BaseAggregateRoot;
import java.util.List;

public class PromptTemplate extends BaseAggregateRoot {

    private final PromptTemplateId promptTemplateId;
    private String useCase;
    private String name;
    private int templateVersion;
    private String systemPrompt;
    private String userPromptTemplate;
    private List<String> variables;
    private String model;
    private double temperature;
    private int maxTokens;
    private boolean active;

    private PromptTemplate(PromptTemplateId promptTemplateId, String useCase, String name, int templateVersion) {
        super(promptTemplateId.value());
        this.promptTemplateId = promptTemplateId;
        this.useCase = useCase;
        this.name = name;
        this.templateVersion = templateVersion;
        this.temperature = 0.7;
        this.maxTokens = 2048;
    }

    public static PromptTemplate create(PromptTemplateId id, String useCase, String name, int templateVersion,
                                          String systemPrompt, String userPromptTemplate) {
        var pt = new PromptTemplate(id, useCase, name, templateVersion);
        pt.systemPrompt = systemPrompt;
        pt.userPromptTemplate = userPromptTemplate;
        return pt;
    }

    public static PromptTemplate reconstitute(PromptTemplateId id, String useCase, String name, int templateVersion,
            String systemPrompt, String userPromptTemplate, List<String> variables, String model,
            double temperature, int maxTokens, boolean active) {
        var pt = new PromptTemplate(id, useCase, name, templateVersion);
        pt.systemPrompt = systemPrompt;
        pt.userPromptTemplate = userPromptTemplate;
        pt.variables = variables;
        pt.model = model;
        pt.temperature = temperature;
        pt.maxTokens = maxTokens;
        pt.active = active;
        return pt;
    }

    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }
    public void updateSettings(String model, double temperature, int maxTokens) {
        this.model = model; this.temperature = temperature; this.maxTokens = maxTokens;
    }

    public PromptTemplateId promptTemplateId() { return promptTemplateId; }
    public String useCase() { return useCase; }
    public String name() { return name; }
    public int templateVersion() { return templateVersion; }
    public String systemPrompt() { return systemPrompt; }
    public String userPromptTemplate() { return userPromptTemplate; }
    public List<String> variables() { return variables; }
    public String model() { return model; }
    public double temperature() { return temperature; }
    public int maxTokens() { return maxTokens; }
    public boolean isActive() { return active; }
}
