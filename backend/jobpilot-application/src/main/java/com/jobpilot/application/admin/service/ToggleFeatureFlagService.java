package com.jobpilot.application.admin.service;

import com.jobpilot.application.admin.dto.FeatureFlagResponse;
import com.jobpilot.application.admin.dto.UpdateFeatureFlagCommand;
import com.jobpilot.application.admin.ports.FeatureFlagRepository;
import com.jobpilot.application.admin.usecase.ToggleFeatureUseCase;
import com.jobpilot.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class ToggleFeatureFlagService implements ToggleFeatureUseCase {

    private final FeatureFlagRepository featureFlagRepository;

    public ToggleFeatureFlagService(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    @Override
    public FeatureFlagResponse execute(UpdateFeatureFlagCommand command) {
        var flag = featureFlagRepository.findByKey(command.key())
            .orElseThrow(() -> new NotFoundException("FeatureFlag", command.key()));
        flag.setEnabled(command.enabled());
        featureFlagRepository.save(flag);
        return FeatureFlagResponse.from(flag, Instant.now());
    }
}
