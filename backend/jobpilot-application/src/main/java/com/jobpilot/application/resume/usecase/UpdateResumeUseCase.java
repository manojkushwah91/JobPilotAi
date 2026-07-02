package com.jobpilot.application.resume.usecase;

import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.resume.dto.UpdateResumeCommand;
import com.jobpilot.application.shared.UseCase;

public interface UpdateResumeUseCase extends UseCase<UpdateResumeCommand, ResumeResponse> {
}
