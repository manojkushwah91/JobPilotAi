package com.jobpilot.application.identity;

import com.jobpilot.application.identity.dto.ChangePasswordCommand;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.service.ChangePasswordService;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.PasswordHash;
import com.jobpilot.domain.identity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChangePasswordService changePasswordService;

    @Test
    void shouldChangePasswordSuccessfully() {
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, "Test User", hash);
        var userId = user.userId().value().toString();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");

        var command = new ChangePasswordCommand(userId, "OldPass1!@", "NewValidPass1!@", "NewValidPass1!@");
        changePasswordService.execute(command);

        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectWrongCurrentPassword() {
        var email = Email.from("test@example.com");
        var hash = PasswordHash.from("$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwx123");
        var user = User.register(email, "Test User", hash);
        var userId = user.userId().value().toString();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        var command = new ChangePasswordCommand(userId, "WrongPass1!@", "NewValidPass1!@", "NewValidPass1!@");
        assertThatThrownBy(() -> changePasswordService.execute(command))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        var command = new ChangePasswordCommand(
            java.util.UUID.randomUUID().toString(),
            "OldPass1!@", "NewValidPass1!@", "NewValidPass1!@"
        );
        assertThatThrownBy(() -> changePasswordService.execute(command))
            .isInstanceOf(UnauthorizedException.class);
    }
}
