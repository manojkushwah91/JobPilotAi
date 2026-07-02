package com.jobpilot.application.interview.usecase;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.interview.dto.ScheduleInterviewCommand;
import com.jobpilot.application.shared.UseCase;

public interface ScheduleInterviewUseCase extends UseCase<ScheduleInterviewCommand, InterviewResponse> {}
