package com.jobpilot.application.resume.usecase;

import com.jobpilot.application.resume.dto.CreateResumeCommand;
import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.shared.UseCase;

public interface CreateResumeUseCase extends UseCase<CreateResumeCommand, ResumeResponse> {
}
