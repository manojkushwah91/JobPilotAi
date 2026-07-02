package com.jobpilot.application.identity.usecase;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.RefreshTokenCommand;
import com.jobpilot.application.shared.UseCase;

public interface RefreshTokenUseCase extends UseCase<RefreshTokenCommand, AuthResponse> {
}
