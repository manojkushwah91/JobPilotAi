package com.jobpilot.application.analytics.dto;

import java.util.List;
import java.util.Map;

public record AnalyticsResponse(
    long totalUsers,
    long activeUsers,
    long totalApplications,
    long totalJobsTracked,
    long totalInterviews,
    Map<String, Long> applicationsByStatus,
    Map<String, Long> jobsBySource,
    Map<String, Long> aiUsageByUseCase,
    List<MonthlyTrend> monthlyTrends
) {}
