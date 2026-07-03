package com.jobpilot.interfaces.rest.v1.auth;

import com.jobpilot.application.identity.dto.AuthenticateCommand;
import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.ChangePasswordCommand;
import com.jobpilot.application.identity.dto.LogoutCommand;
import com.jobpilot.application.identity.dto.OAuthCommand;
import com.jobpilot.application.identity.dto.RefreshTokenCommand;
import com.jobpilot.application.identity.dto.RegisterUserCommand;
import com.jobpilot.application.identity.ports.EmailVerificationTokenRepository;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.PasswordResetTokenRepository;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.service.OAuthService;
import com.jobpilot.application.identity.usecase.AuthenticateUserUseCase;
import com.jobpilot.application.identity.usecase.ChangePasswordUseCase;
import com.jobpilot.application.identity.usecase.LogoutUseCase;
import com.jobpilot.application.identity.usecase.RefreshTokenUseCase;
import com.jobpilot.application.identity.usecase.RegisterUserUseCase;
import com.jobpilot.application.notification.ports.EmailSenderPort;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UserRepository userRepository;
    private final EmailSenderPort emailSender;
    private final OAuthService oauthService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          ChangePasswordUseCase changePasswordUseCase,
                          UserRepository userRepository,
                          EmailSenderPort emailSender,
                          OAuthService oauthService,
                          EmailVerificationTokenRepository emailVerificationTokenRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          PasswordEncoder passwordEncoder) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.oauthService = oauthService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
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

    @RateLimited(capacity = 5)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        var userOpt = userRepository.findByEmail(com.jobpilot.domain.identity.Email.from(request.email()));
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            var token = UUID.randomUUID().toString();
            var resetToken = com.jobpilot.domain.identity.PasswordResetToken.create(user.userId().value(), token, java.time.Duration.ofHours(1));
            passwordResetTokenRepository.save(resetToken);
            emailSender.sendWithTemplate(request.email(), "password-reset", Map.of(
                "name", user.email().value().split("@")[0],
                "resetLink", "http://localhost:3000/reset-password?token=" + token
            ));
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 5)
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        var tokenOpt = passwordResetTokenRepository.findByToken(request.token());
        if (tokenOpt.isEmpty()) {
            throw new com.jobpilot.common.exception.NotFoundException("PasswordResetToken", request.token());
        }
        var resetToken = tokenOpt.get();
        if (!resetToken.isValid()) {
            throw new com.jobpilot.common.exception.ValidationException("token", "Token expired or already used");
        }
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new com.jobpilot.common.exception.ValidationException("confirmNewPassword", "Passwords do not match");
        }
        var userOpt = userRepository.findById(com.jobpilot.domain.identity.UserId.from(resetToken.userId()));
        if (userOpt.isEmpty()) {
            throw new com.jobpilot.common.exception.NotFoundException("User", resetToken.userId());
        }
        var user = userOpt.get();
        var encodedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(com.jobpilot.domain.identity.PasswordHash.from(encodedPassword));
        userRepository.save(user);
        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RateLimited(capacity = 10)
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        var tokenOpt = emailVerificationTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            throw new com.jobpilot.common.exception.NotFoundException("EmailVerificationToken", token);
        }
        var verificationToken = tokenOpt.get();
        if (!verificationToken.isValid()) {
            throw new com.jobpilot.common.exception.ValidationException("token", "Token expired or already used");
        }
        var userOpt = userRepository.findById(com.jobpilot.domain.identity.UserId.from(verificationToken.userId()));
        if (userOpt.isEmpty()) {
            throw new com.jobpilot.common.exception.NotFoundException("User", verificationToken.userId());
        }
        var user = userOpt.get();
        user.verifyEmail();
        userRepository.save(user);
        verificationToken.markUsed();
        emailVerificationTokenRepository.save(verificationToken);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/oauth/{provider}")
    public ResponseEntity<Void> initiateOAuth(@PathVariable String provider) {
        var authUrl = oauthService.initiateOAuth(provider);
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, authUrl)
            .build();
    }

    @PostMapping("/oauth/{provider}/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> handleOAuthCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam(defaultValue = "http://localhost:3000/auth/callback") String redirectUri) {
        var response = oauthService.handleOAuthCallback(new OAuthCommand(provider, code, redirectUri));
        return ResponseEntity.ok(ApiResponse.ok(response));
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

    public record ForgotPasswordRequest(
        @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 12, max = 128) String newPassword,
        @NotBlank String confirmNewPassword
    ) {}
}
