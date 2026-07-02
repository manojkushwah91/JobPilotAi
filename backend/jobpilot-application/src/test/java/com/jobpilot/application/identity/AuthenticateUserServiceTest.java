package com.jobpilot.application.identity;

import com.jobpilot.application.identity.dto.AuthenticateCommand;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.service.AuthenticateUserService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthenticateUserService authenticateUserService;

    @Test
    void shouldAuthenticateSuccessfully() {
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, hash);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any())).thenReturn("refresh-token");
        when(tokenProvider.getExpirationFromToken(any())).thenReturn(Instant.now().plusSeconds(900));

        var command = new AuthenticateCommand("test@example.com", "ValidPass1!");
        var response = authenticateUserService.execute(command);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldRejectInvalidEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        var command = new AuthenticateCommand("unknown@example.com", "ValidPass1!");
        assertThatThrownBy(() -> authenticateUserService.execute(command))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shouldRejectWrongPassword() {
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, hash);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        var command = new AuthenticateCommand("test@example.com", "WrongPass1!");
        assertThatThrownBy(() -> authenticateUserService.execute(command))
            .isInstanceOf(UnauthorizedException.class);
    }
}
