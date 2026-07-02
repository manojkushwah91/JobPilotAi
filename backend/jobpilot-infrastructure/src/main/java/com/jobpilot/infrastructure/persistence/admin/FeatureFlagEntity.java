package com.jobpilot.infrastructure.persistence.admin;

import com.jobpilot.domain.admin.FeatureFlag;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feature_flags")
public class FeatureFlagEntity extends BaseJpaEntity {

    @Id @Column(name = "flag_key") private String flagKey;
    @Column(name = "enabled", nullable = false) private boolean enabled;
    @Column(name = "description") private String description;

    protected FeatureFlagEntity() {}

    public static FeatureFlagEntity fromDomain(FeatureFlag flag) {
        var e = new FeatureFlagEntity();
        e.flagKey = flag.getKey();
        e.enabled = flag.isEnabled();
        e.description = flag.getDescription();
        return e;
    }

    public FeatureFlag toDomain() {
        return new FeatureFlag(flagKey, enabled, description);
    }

    public String getKey() { return flagKey; }
    public boolean isEnabled() { return enabled; }
    public String getDescription() { return description; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setDescription(String description) { this.description = description; }
}
