package com.jobpilot.application.resume.usecase;

import com.jobpilot.application.resume.dto.GetResumeCommand;
import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.shared.UseCase;

public interface GetResumeUseCase extends UseCase<GetResumeCommand, ResumeResponse> {
}
