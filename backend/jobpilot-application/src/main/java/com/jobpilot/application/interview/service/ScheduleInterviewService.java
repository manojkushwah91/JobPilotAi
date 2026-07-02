package com.jobpilot.application.interview.service;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.interview.dto.ScheduleInterviewCommand;
import com.jobpilot.application.interview.ports.InterviewRepository;
import com.jobpilot.application.interview.usecase.ScheduleInterviewUseCase;
import com.jobpilot.domain.interview.InterviewSession;
import com.jobpilot.domain.interview.InterviewSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleInterviewService implements ScheduleInterviewUseCase {

    private final InterviewRepository interviewRepository;

    public ScheduleInterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Override
    public InterviewResponse execute(ScheduleInterviewCommand command) {
        var sessionId = InterviewSessionId.generate();
        var session = InterviewSession.schedule(sessionId, command.userId(), command.companyId(),
            command.jobId(), command.type(), command.scheduledAt());
        session.updateDetails(command.durationMinutes(), command.interviewerName(),
            command.location(), command.meetingLink(), command.notes());
        interviewRepository.save(session);
        return InterviewResponse.from(session);
    }
}
