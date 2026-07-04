package com.jobpilot.application.identity.service;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.RegisterUserCommand;
import com.jobpilot.application.identity.ports.EmailVerificationTokenRepository;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.notification.ports.EmailSenderPort;
import com.jobpilot.application.identity.usecase.RegisterUserUseCase;
import com.jobpilot.common.exception.DuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.PasswordHash;
import com.jobpilot.domain.identity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobpilot.domain.identity.EmailVerificationToken;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger logger = LoggerFactory.getLogger(RegisterUserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailSenderPort emailSender;

    public RegisterUserService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                TokenProvider tokenProvider,
                                EmailVerificationTokenRepository emailVerificationTokenRepository,
                                EmailSenderPort emailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailSender = emailSender;
    }

    @Override
    public AuthResponse execute(RegisterUserCommand command) {
        var email = Email.from(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateException("User", "email", command.email());
        }

        var encodedPassword = passwordEncoder.encode(command.password());
        var passwordHash = PasswordHash.from(encodedPassword);
        var user = User.register(email, command.name(), passwordHash);

        userRepository.save(user);

        var token = UUID.randomUUID().toString();
        var verificationToken = EmailVerificationToken.create(user.userId().value(), token, Duration.ofHours(24));
        emailVerificationTokenRepository.save(verificationToken);
        try {
            emailSender.sendWithTemplate(user.email().value(), "verify-email", Map.of(
                "name", user.email().value().split("@")[0],
                "verifyLink", "http://localhost:3000/verify-email?token=" + token
            ));
        } catch (Exception e) {
            // Email sending is non-critical; registration succeeds even if mail server is unavailable
            logger.warn("Failed to send verification email to {}: {}", user.email().value(), e.getMessage());
        }

        var accessToken = tokenProvider.generateAccessToken(
            user.userId().value().toString(),
            user.email().value(),
            List.of(user.role().name()),
            user.role().name()
        );

        var refreshToken = tokenProvider.generateRefreshToken(
            user.userId().value().toString(),
            user.userId().value().toString()
        );

        var expiresIn = tokenProvider.getExpirationFromToken(accessToken).toEpochMilli();
        var userResponse = new AuthResponse.UserResponse(
            user.userId().value().toString(),
            user.email().value(),
            user.role().name(),
            user.isEmailVerified()
        );

        return AuthResponse.of(accessToken, refreshToken, expiresIn, userResponse);
    }
}
