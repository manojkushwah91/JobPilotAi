package com.jobpilot.application.admin.ports;

import com.jobpilot.domain.admin.FeatureFlag;
import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepository {
    Optional<FeatureFlag> findByKey(String key);
    List<FeatureFlag> findAll();
    FeatureFlag save(FeatureFlag flag);
}
