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
}
