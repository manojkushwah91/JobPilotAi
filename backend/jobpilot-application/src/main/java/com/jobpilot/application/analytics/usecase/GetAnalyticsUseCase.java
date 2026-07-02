package com.jobpilot.application.analytics.usecase;

import com.jobpilot.application.analytics.dto.AnalyticsResponse;
import com.jobpilot.application.analytics.dto.DateRangeCommand;
import com.jobpilot.application.shared.UseCase;

public interface GetAnalyticsUseCase extends UseCase<DateRangeCommand, AnalyticsResponse> {}
