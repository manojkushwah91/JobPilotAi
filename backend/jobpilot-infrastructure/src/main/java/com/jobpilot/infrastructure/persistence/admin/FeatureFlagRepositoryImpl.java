package com.jobpilot.infrastructure.persistence.admin;

import com.jobpilot.application.admin.ports.FeatureFlagRepository;
import com.jobpilot.domain.admin.FeatureFlag;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FeatureFlagRepositoryImpl implements FeatureFlagRepository {

    private final FeatureFlagJpaRepository jpaRepository;

    public FeatureFlagRepositoryImpl(FeatureFlagJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<FeatureFlag> findByKey(String key) {
        return jpaRepository.findById(key).map(FeatureFlagEntity::toDomain);
    }

    @Override
    public List<FeatureFlag> findAll() {
        return jpaRepository.findAll().stream().map(FeatureFlagEntity::toDomain).toList();
    }

    @Override
    public FeatureFlag save(FeatureFlag flag) {
        return jpaRepository.save(FeatureFlagEntity.fromDomain(flag)).toDomain();
    }
}
