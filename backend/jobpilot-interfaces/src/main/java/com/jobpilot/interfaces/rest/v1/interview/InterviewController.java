package com.jobpilot.interfaces.rest.v1.interview;

import com.jobpilot.application.interview.dto.*;
import com.jobpilot.application.interview.usecase.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final ScheduleInterviewUseCase scheduleInterviewUseCase;
    private final CompleteInterviewUseCase completeInterviewUseCase;
    private final CancelInterviewUseCase cancelInterviewUseCase;
    private final GetInterviewUseCase getInterviewUseCase;
    private final ListUserInterviewsUseCase listUserInterviewsUseCase;

    public InterviewController(ScheduleInterviewUseCase scheduleInterviewUseCase,
                                CompleteInterviewUseCase completeInterviewUseCase,
                                CancelInterviewUseCase cancelInterviewUseCase,
                                GetInterviewUseCase getInterviewUseCase,
                                ListUserInterviewsUseCase listUserInterviewsUseCase) {
        this.scheduleInterviewUseCase = scheduleInterviewUseCase;
        this.completeInterviewUseCase = completeInterviewUseCase;
        this.cancelInterviewUseCase = cancelInterviewUseCase;
        this.getInterviewUseCase = getInterviewUseCase;
        this.listUserInterviewsUseCase = listUserInterviewsUseCase;
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<InterviewResponse>> schedule(@Valid @RequestBody ScheduleRequest request) {
        var command = new ScheduleInterviewCommand(UUID.fromString(request.userId()), request.companyId(),
            request.jobId(), request.type(), request.scheduledAt(), request.durationMinutes(),
            request.interviewerName(), request.interviewRound(), request.location(),
            request.meetingLink(), request.notes());
        var response = scheduleInterviewUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> getById(@PathVariable String id) {
        var response = getInterviewUseCase.execute(new GetInterviewCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> listByUser(@PathVariable UUID userId) {
        var response = listUserInterviewsUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<InterviewResponse>> complete(@PathVariable UUID id,
            @Valid @RequestBody CompleteRequest request) {
        var response = completeInterviewUseCase.execute(new CompleteInterviewCommand(id, request.rating(), request.feedback()));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable String id,
            @RequestBody(required = false) CancelRequest request) {
        cancelInterviewUseCase.execute(new CancelInterviewCommand(id, request != null ? request.reason() : null));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record ScheduleRequest(
        @NotBlank String userId, UUID companyId, UUID jobId,
        @NotBlank String type, @NotNull Instant scheduledAt,
        Integer durationMinutes, String interviewerName, Integer interviewRound,
        String location, String meetingLink, String notes
    ) {}

    public record CompleteRequest(@NotNull int rating, String feedback) {}

    public record CancelRequest(String reason) {}
}
