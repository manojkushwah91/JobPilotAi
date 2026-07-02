package com.jobpilot.application.identity.service;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.RegisterUserCommand;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.usecase.RegisterUserUseCase;
import com.jobpilot.common.exception.DuplicateException;
import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.PasswordHash;
import com.jobpilot.domain.identity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public RegisterUserService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse execute(RegisterUserCommand command) {
        var email = Email.from(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateException("User", "email", command.email());
        }

        var encodedPassword = passwordEncoder.encode(command.password());
        var passwordHash = PasswordHash.from(encodedPassword);
        var user = User.register(email, passwordHash);

        userRepository.save(user);

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
