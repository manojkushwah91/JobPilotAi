package com.jobpilot.interfaces.rest.v1.user;

import com.jobpilot.application.user.dto.UpdateProfileCommand;
import com.jobpilot.application.user.dto.UpdateSettingsCommand;
import com.jobpilot.application.user.dto.UserProfileResponse;
import com.jobpilot.application.user.dto.UserSettingsResponse;
import com.jobpilot.application.user.service.DeleteUserService;
import com.jobpilot.application.user.service.GetUserProfileService;
import com.jobpilot.application.user.service.GetUserSettingsService;
import com.jobpilot.application.user.service.UpdateUserProfileService;
import com.jobpilot.application.user.service.UpdateUserSettingsService;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserProfileService getUserProfileService;
    private final UpdateUserProfileService updateUserProfileService;
    private final GetUserSettingsService getUserSettingsService;
    private final UpdateUserSettingsService updateUserSettingsService;
    private final DeleteUserService deleteUserService;

    public UserController(GetUserProfileService getUserProfileService,
                          UpdateUserProfileService updateUserProfileService,
                          GetUserSettingsService getUserSettingsService,
                          UpdateUserSettingsService updateUserSettingsService,
                          DeleteUserService deleteUserService) {
        this.getUserProfileService = getUserProfileService;
        this.updateUserProfileService = updateUserProfileService;
        this.getUserSettingsService = getUserSettingsService;
        this.updateUserSettingsService = updateUserSettingsService;
        this.deleteUserService = deleteUserService;
    }

    @RateLimited(capacity = 100)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var response = getUserProfileService.execute(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new UpdateProfileCommand(
            UUID.fromString(principal.userId()),
            request.name(),
            request.avatarUrl(),
            request.locale()
        );
        var response = updateUserProfileService.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getSettings(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var response = getUserSettingsService.execute(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PutMapping("/me/settings")
    public ResponseEntity<ApiResponse<Void>> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new UpdateSettingsCommand(
            UUID.fromString(principal.userId()),
            request.jobPreferences(),
            request.notificationPrefs(),
            request.privacySettings(),
            request.aiPreferences(),
            request.appearance()
        );
        updateUserSettingsService.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 100)
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        deleteUserService.execute(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record UpdateProfileRequest(
        @Size(max = 255) String name,
        @Size(max = 512) String avatarUrl,
        @Size(max = 10) String locale
    ) {}

    public record UpdateSettingsRequest(
        Map<String, Object> jobPreferences,
        Map<String, Object> notificationPrefs,
        Map<String, Object> privacySettings,
        Map<String, Object> aiPreferences,
        Map<String, Object> appearance
    ) {}
}
