package com.jobpilot.interfaces.rest.v1.notification;

import com.jobpilot.application.notification.dto.*;
import com.jobpilot.application.notification.usecase.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.jobpilot.domain.notification.NotificationChannel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final GetNotificationUseCase getNotificationUseCase;
    private final MarkAsReadUseCase markAsReadUseCase;

    public NotificationController(SendNotificationUseCase sendNotificationUseCase,
                                   GetNotificationUseCase getNotificationUseCase,
                                   MarkAsReadUseCase markAsReadUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.getNotificationUseCase = getNotificationUseCase;
        this.markAsReadUseCase = markAsReadUseCase;
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> send(@Valid @RequestBody SendRequest request) {
        var command = new SendNotificationCommand(UUID.fromString(request.userId()), request.type(),
            request.channel(), request.title(), request.body(), request.metadata());
        var response = sendNotificationUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable String id) {
        var response = getNotificationUseCase.execute(new GetNotificationCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable String id) {
        markAsReadUseCase.execute(new MarkAsReadCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record SendRequest(
        @NotBlank String userId, @NotBlank String type, @NotNull NotificationChannel channel,
        @NotBlank String title, @NotBlank String body, Map<String, Object> metadata
    ) {}
}
