package com.jobpilot.application.identity;

import com.jobpilot.application.identity.dto.RegisterUserCommand;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.service.RegisterUserService;
import com.jobpilot.common.exception.DuplicateException;
import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    void shouldRegisterUserSuccessfully() {
        var command = new RegisterUserCommand("test@example.com", "ValidPass1!@", "ValidPass1!@");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenProvider.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any())).thenReturn("refresh-token");
        when(tokenProvider.getExpirationFromToken(any())).thenReturn(Instant.now().plusSeconds(900));

        var response = registerUserService.execute(command);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user()).isNotNull();
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        var command = new RegisterUserCommand("existing@example.com", "ValidPass1!@", "ValidPass1!@");

        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> registerUserService.execute(command))
            .isInstanceOf(DuplicateException.class);
    }
}
