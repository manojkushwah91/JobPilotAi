package com.jobpilot.interfaces.rest.v1.auth;

import com.jobpilot.application.identity.dto.AuthenticateCommand;
import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.ChangePasswordCommand;
import com.jobpilot.application.identity.dto.LogoutCommand;
import com.jobpilot.application.identity.dto.RefreshTokenCommand;
import com.jobpilot.application.identity.dto.RegisterUserCommand;
import com.jobpilot.application.identity.usecase.AuthenticateUserUseCase;
import com.jobpilot.application.identity.usecase.ChangePasswordUseCase;
import com.jobpilot.application.identity.usecase.LogoutUseCase;
import com.jobpilot.application.identity.usecase.RefreshTokenUseCase;
import com.jobpilot.application.identity.usecase.RegisterUserUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          ChangePasswordUseCase changePasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @RateLimited(capacity = 10)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var command = new RegisterUserCommand(request.email(), request.password(), request.confirmPassword());
        var response = registerUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 10)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        var command = new AuthenticateCommand(request.email(), request.password());
        var response = authenticateUserUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 10)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var command = new RefreshTokenCommand(request.refreshToken());
        var response = refreshTokenUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 10)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) {
        var command = new LogoutCommand(request.accessToken(), request.refreshToken());
        logoutUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 10)
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var command = new ChangePasswordCommand(
            principal.userId(), request.currentPassword(),
            request.newPassword(), request.confirmNewPassword());
        changePasswordUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotBlank String confirmPassword
    ) {}

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    public record RefreshTokenRequest(
        @NotBlank String refreshToken
    ) {}

    public record LogoutRequest(
        String accessToken,
        String refreshToken
    ) {}

    public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 12, max = 128) String newPassword,
        @NotBlank String confirmNewPassword
    ) {}
}
