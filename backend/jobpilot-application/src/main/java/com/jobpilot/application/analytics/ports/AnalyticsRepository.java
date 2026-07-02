package com.jobpilot.application.analytics.ports;

import com.jobpilot.application.analytics.dto.DateRangeCommand;
import com.jobpilot.application.analytics.dto.MonthlyTrend;

import java.util.List;
import java.util.Map;

public interface AnalyticsRepository {
    long countTotalUsers();
    long countActiveUsers(DateRangeCommand range);
    long countTotalApplications();
    long countApplicationsByDateRange(DateRangeCommand range);
    long countTotalJobs();
    long countTotalInterviews();
    Map<String, Long> applicationsByStatus();
    Map<String, Long> jobsBySource();
    Map<String, Long> aiUsageByUseCase(DateRangeCommand range);
    List<MonthlyTrend> monthlyTrends(DateRangeCommand range);
}
