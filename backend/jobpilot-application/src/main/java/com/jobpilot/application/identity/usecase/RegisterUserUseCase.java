package com.jobpilot.application.identity.usecase;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.RegisterUserCommand;
import com.jobpilot.application.shared.UseCase;

public interface RegisterUserUseCase extends UseCase<RegisterUserCommand, AuthResponse> {
}
