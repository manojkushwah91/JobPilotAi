package com.jobpilot.application.identity;

import com.jobpilot.application.identity.dto.RefreshTokenCommand;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.service.RefreshTokenService;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.PasswordHash;
import com.jobpilot.domain.identity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldRefreshTokensSuccessfully() {
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, "Test User", hash);
        var userId = user.userId().value().toString();

        when(tokenProvider.validateToken(any())).thenReturn(true);
        when(tokenProvider.getTokenType(any())).thenReturn("refresh");
        when(tokenProvider.getUserIdFromToken(any())).thenReturn(userId);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(any(), any(), any(), any())).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(any(), any())).thenReturn("new-refresh-token");
        when(tokenProvider.getExpirationFromToken(any())).thenReturn(Instant.now().plusSeconds(900));

        var command = new RefreshTokenCommand("valid-refresh-token");
        var response = refreshTokenService.execute(command);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldRejectInvalidToken() {
        when(tokenProvider.validateToken(any())).thenReturn(false);

        var command = new RefreshTokenCommand("invalid-token");
        assertThatThrownBy(() -> refreshTokenService.execute(command))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shouldRejectWrongTokenType() {
        when(tokenProvider.validateToken(any())).thenReturn(true);
        when(tokenProvider.getTokenType(any())).thenReturn("access");

        var command = new RefreshTokenCommand("access-token");
        assertThatThrownBy(() -> refreshTokenService.execute(command))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shouldRejectDeletedUser() {
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, "Test User", hash);
        user.softDelete();
        var userId = user.userId().value().toString();

        when(tokenProvider.validateToken(any())).thenReturn(true);
        when(tokenProvider.getTokenType(any())).thenReturn("refresh");
        when(tokenProvider.getUserIdFromToken(any())).thenReturn(userId);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        var command = new RefreshTokenCommand("valid-refresh-token");
        assertThatThrownBy(() -> refreshTokenService.execute(command))
            .isInstanceOf(UnauthorizedException.class);
    }
}
