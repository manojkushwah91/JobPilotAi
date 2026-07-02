package com.jobpilot.application.identity.service;

import com.jobpilot.application.identity.dto.LogoutCommand;
import com.jobpilot.application.identity.ports.RevokedTokenStore;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.usecase.LogoutUseCase;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutUseCase {

    private final TokenProvider tokenProvider;
    private final RevokedTokenStore revokedTokenStore;

    public LogoutService(TokenProvider tokenProvider, RevokedTokenStore revokedTokenStore) {
        this.tokenProvider = tokenProvider;
        this.revokedTokenStore = revokedTokenStore;
    }

    @Override
    public Void execute(LogoutCommand command) {
        if (command.accessToken() != null && tokenProvider.validateToken(command.accessToken())) {
            revokedTokenStore.revoke(command.accessToken());
        }
        if (command.refreshToken() != null && tokenProvider.validateToken(command.refreshToken())) {
            revokedTokenStore.revoke(command.refreshToken());
        }
        return null;
    }
}
