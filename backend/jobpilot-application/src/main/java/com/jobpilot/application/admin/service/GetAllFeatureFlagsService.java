package com.jobpilot.application.admin.service;

import com.jobpilot.application.admin.dto.FeatureFlagResponse;
import com.jobpilot.application.admin.ports.FeatureFlagRepository;
import com.jobpilot.application.admin.usecase.GetAllFlagsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class GetAllFeatureFlagsService implements GetAllFlagsUseCase {

    private final FeatureFlagRepository featureFlagRepository;

    public GetAllFeatureFlagsService(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    @Override
    public List<FeatureFlagResponse> execute(Void input) {
        return featureFlagRepository.findAll().stream()
            .map(f -> FeatureFlagResponse.from(f, Instant.now()))
            .toList();
    }
}
