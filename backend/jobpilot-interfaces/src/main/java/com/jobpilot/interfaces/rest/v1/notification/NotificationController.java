package com.jobpilot.interfaces.rest.v1.notification;

import com.jobpilot.application.notification.dto.*;
import com.jobpilot.application.notification.ports.NotificationRepository;
import com.jobpilot.application.notification.usecase.*;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.jobpilot.domain.notification.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final GetNotificationUseCase getNotificationUseCase;
    private final MarkAsReadUseCase markAsReadUseCase;
    private final NotificationRepository notificationRepository;

    public NotificationController(SendNotificationUseCase sendNotificationUseCase,
                                   GetNotificationUseCase getNotificationUseCase,
                                   MarkAsReadUseCase markAsReadUseCase,
                                   NotificationRepository notificationRepository) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.getNotificationUseCase = getNotificationUseCase;
        this.markAsReadUseCase = markAsReadUseCase;
        this.notificationRepository = notificationRepository;
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
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(
            @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var response = getNotificationUseCase.execute(new GetNotificationCommand(id));
        if (!response.userId().equals(UUID.fromString(principal.userId()))) throw new UnauthorizedException("Access denied");
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable String id) {
        markAsReadUseCase.execute(new MarkAsReadCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> list(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = notificationRepository.findByUserId(UUID.fromString(principal.userId()), PageRequest.of(page, size))
            .map(NotificationResponse::from);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var count = notificationRepository.countUnreadByUserId(UUID.fromString(principal.userId()));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    @RateLimited(capacity = 50)
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal JwtPrincipal principal) {
        notificationRepository.markAllReadByUserId(UUID.fromString(principal.userId()));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<Map<String, Object>>> preferences() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "email", true,
            "inApp", true,
            "push", false,
            "jobAlerts", true,
            "applicationUpdates", true
        )));
    }

    public record SendRequest(
        @NotBlank String userId, @NotBlank String type, @NotNull NotificationChannel channel,
        @NotBlank String title, @NotBlank String body, Map<String, Object> metadata
    ) {}
}
