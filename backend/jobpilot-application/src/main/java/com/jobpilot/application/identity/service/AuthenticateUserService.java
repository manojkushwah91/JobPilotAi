package com.jobpilot.application.identity.service;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.AuthenticateCommand;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.usecase.AuthenticateUserUseCase;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.domain.identity.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthenticateUserService(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse execute(AuthenticateCommand command) {
        var email = Email.from(command.email());
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.isDeleted()) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!passwordEncoder.matches(command.password(), user.passwordHash().value())) {
            throw new UnauthorizedException("Invalid email or password");
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
