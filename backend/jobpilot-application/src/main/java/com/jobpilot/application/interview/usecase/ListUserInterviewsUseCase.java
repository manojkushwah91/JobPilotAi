package com.jobpilot.application.interview.usecase;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.shared.UseCase;

import java.util.UUID;

public interface ListUserInterviewsUseCase extends UseCase<UUID, java.util.List<InterviewResponse>> {}
