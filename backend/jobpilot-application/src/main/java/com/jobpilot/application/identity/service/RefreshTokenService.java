package com.jobpilot.application.identity.service;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.RefreshTokenCommand;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.usecase.RefreshTokenUseCase;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public RefreshTokenService(TokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public AuthResponse execute(RefreshTokenCommand command) {
        if (!tokenProvider.validateToken(command.refreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        var tokenType = tokenProvider.getTokenType(command.refreshToken());
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }

        var userId = UserId.from(java.util.UUID.fromString(
            tokenProvider.getUserIdFromToken(command.refreshToken())));

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.isDeleted()) {
            throw new UnauthorizedException("User account is deactivated");
        }

        var newAccessToken = tokenProvider.generateAccessToken(
            user.userId().value().toString(),
            user.email().value(),
            List.of(user.role().name()),
            user.role().name()
        );

        var newRefreshToken = tokenProvider.generateRefreshToken(
            user.userId().value().toString(),
            user.userId().value().toString()
        );

        var expiresIn = tokenProvider.getExpirationFromToken(newAccessToken).toEpochMilli();
        var userResponse = new AuthResponse.UserResponse(
            user.userId().value().toString(),
            user.email().value(),
            user.role().name(),
            user.isEmailVerified()
        );

        return AuthResponse.of(newAccessToken, newRefreshToken, expiresIn, userResponse);
    }
}
