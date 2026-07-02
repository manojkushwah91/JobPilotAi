package com.jobpilot.application.analytics.service;

import com.jobpilot.application.analytics.dto.AnalyticsResponse;
import com.jobpilot.application.analytics.dto.DateRangeCommand;
import com.jobpilot.application.analytics.ports.AnalyticsRepository;
import com.jobpilot.application.analytics.usecase.GetAnalyticsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService implements GetAnalyticsUseCase {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public AnalyticsResponse execute(DateRangeCommand command) {
        return new AnalyticsResponse(
            analyticsRepository.countTotalUsers(),
            analyticsRepository.countActiveUsers(command),
            analyticsRepository.countTotalApplications(),
            analyticsRepository.countTotalJobs(),
            analyticsRepository.countTotalInterviews(),
            analyticsRepository.applicationsByStatus(),
            analyticsRepository.jobsBySource(),
            analyticsRepository.aiUsageByUseCase(command),
            analyticsRepository.monthlyTrends(command)
        );
    }
}
