package com.jobpilot.application.identity.usecase;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.AuthenticateCommand;
import com.jobpilot.application.shared.UseCase;

public interface AuthenticateUserUseCase extends UseCase<AuthenticateCommand, AuthResponse> {
}
