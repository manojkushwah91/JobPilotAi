package com.jobpilot.application.identity;

import com.jobpilot.application.identity.dto.LogoutCommand;
import com.jobpilot.application.identity.ports.RevokedTokenStore;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.service.LogoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RevokedTokenStore revokedTokenStore;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void shouldRevokeAccessToken() {
        when(tokenProvider.validateToken(any())).thenReturn(true);

        var command = new LogoutCommand("valid-access-token", null);
        logoutService.execute(command);

        verify(revokedTokenStore).revoke("valid-access-token");
    }

    @Test
    void shouldRevokeRefreshToken() {
        when(tokenProvider.validateToken(any())).thenReturn(true);

        var command = new LogoutCommand(null, "valid-refresh-token");
        logoutService.execute(command);

        verify(revokedTokenStore).revoke("valid-refresh-token");
    }

    @Test
    void shouldRevokeBothTokens() {
        when(tokenProvider.validateToken(any())).thenReturn(true);

        var command = new LogoutCommand("valid-access-token", "valid-refresh-token");
        logoutService.execute(command);

        verify(revokedTokenStore).revoke("valid-access-token");
        verify(revokedTokenStore).revoke("valid-refresh-token");
    }

    @Test
    void shouldSkipInvalidToken() {
        when(tokenProvider.validateToken(any())).thenReturn(false);

        var command = new LogoutCommand("invalid-token", null);
        logoutService.execute(command);

        verifyNoInteractions(revokedTokenStore);
    }
}
