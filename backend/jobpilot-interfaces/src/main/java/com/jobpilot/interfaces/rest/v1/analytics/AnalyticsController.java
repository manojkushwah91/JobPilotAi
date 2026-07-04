package com.jobpilot.interfaces.rest.v1.analytics;

import com.jobpilot.application.analytics.dto.AnalyticsResponse;
import com.jobpilot.application.analytics.dto.DateRangeCommand;
import com.jobpilot.application.analytics.usecase.GetAnalyticsUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final GetAnalyticsUseCase getAnalyticsUseCase;

    public AnalyticsController(GetAnalyticsUseCase getAnalyticsUseCase) {
        this.getAnalyticsUseCase = getAnalyticsUseCase;
    }

    @RateLimited(capacity = 20)
    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        return ResponseEntity.ok(ApiResponse.ok(getAnalyticsUseCase.execute(new DateRangeCommand(startDate, endDate))));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(
            @RequestParam(defaultValue = "30d") String period) {
        Instant endDate = Instant.now();
        Instant startDate = switch (period) {
            case "7d" -> endDate.minus(7, ChronoUnit.DAYS);
            case "90d" -> endDate.minus(90, ChronoUnit.DAYS);
            default -> endDate.minus(30, ChronoUnit.DAYS);
        };
        AnalyticsResponse resp = getAnalyticsUseCase.execute(new DateRangeCommand(startDate, endDate));
        Map<String, Object> overview = new java.util.HashMap<>();
        overview.put("totalApplications", resp.totalApplications());
        overview.put("totalInterviews", resp.totalInterviews());
        overview.put("jobOffers", 0L);
        overview.put("aiCalls", resp.aiUsageByUseCase().values().stream().mapToLong(Long::longValue).sum());
        overview.put("activeUsers", resp.activeUsers());
        overview.put("jobsTracked", resp.totalJobsTracked());
        overview.put("applicationsByStatus", resp.applicationsByStatus());
        overview.put("applicationsOverTime", List.of());
        overview.put("jobsBySource", resp.jobsBySource());
        overview.put("aiUsageByUseCase", resp.aiUsageByUseCase());
        overview.put("skillGaps", List.of());
        overview.put("monthlyTrends", resp.monthlyTrends());
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/application-funnel")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApplicationFunnel(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(List.of(
            Map.of("stage", "SAVED", "count", 0),
            Map.of("stage", "APPLIED", "count", 0),
            Map.of("stage", "SCREENING", "count", 0),
            Map.of("stage", "INTERVIEW", "count", 0),
            Map.of("stage", "OFFER", "count", 0),
            Map.of("stage", "REJECTED", "count", 0)
        )));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/skill-gaps")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSkillGaps() {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/resume-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getResumeTrend(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/market-benchmarks")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMarketBenchmarks() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "avgResponseRate", 0.0, "avgSalaryByRole", Map.of(),
            "topSkills", List.of(), "hiringDemand", Map.of()
        )));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/interview-performance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInterviewPerformance() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "totalInterviews", 0, "avgScore", 0.0,
            "scoreDistribution", Map.of(), "performanceByType", Map.of()
        )));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTimeline(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @RateLimited(capacity = 20)
    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActivity(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }
}
