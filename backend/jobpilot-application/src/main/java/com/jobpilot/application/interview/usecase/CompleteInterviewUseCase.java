package com.jobpilot.application.interview.usecase;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.interview.dto.CompleteInterviewCommand;
import com.jobpilot.application.shared.UseCase;

public interface CompleteInterviewUseCase extends UseCase<CompleteInterviewCommand, InterviewResponse> {}
