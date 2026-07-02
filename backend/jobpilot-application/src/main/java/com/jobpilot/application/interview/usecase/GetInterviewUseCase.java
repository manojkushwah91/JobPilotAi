package com.jobpilot.application.interview.usecase;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.interview.dto.GetInterviewCommand;
import com.jobpilot.application.shared.UseCase;

public interface GetInterviewUseCase extends UseCase<GetInterviewCommand, InterviewResponse> {}
