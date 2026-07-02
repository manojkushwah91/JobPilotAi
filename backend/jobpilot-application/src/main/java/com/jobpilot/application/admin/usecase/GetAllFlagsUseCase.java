package com.jobpilot.application.admin.usecase;

import com.jobpilot.application.admin.dto.FeatureFlagResponse;
import com.jobpilot.application.shared.UseCase;
import java.util.List;

public interface GetAllFlagsUseCase extends UseCase<Void, List<FeatureFlagResponse>> {}
