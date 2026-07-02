package com.jobpilot.application.admin.usecase;

import com.jobpilot.application.admin.dto.FeatureFlagResponse;
import com.jobpilot.application.admin.dto.UpdateFeatureFlagCommand;
import com.jobpilot.application.shared.UseCase;

public interface ToggleFeatureUseCase extends UseCase<UpdateFeatureFlagCommand, FeatureFlagResponse> {}
