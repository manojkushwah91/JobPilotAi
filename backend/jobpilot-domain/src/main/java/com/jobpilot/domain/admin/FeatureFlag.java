package com.jobpilot.domain.admin;

public class FeatureFlag {

    private String key;
    private boolean enabled;
    private String description;

    public FeatureFlag(String key, boolean enabled, String description) {
        this.key = key;
        this.enabled = enabled;
        this.description = description;
    }

    public String getKey() { return key; }
    public boolean isEnabled() { return enabled; }
    public String getDescription() { return description; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setDescription(String description) { this.description = description; }
}
