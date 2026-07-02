package com.jobpilot.application.interview.service;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.interview.dto.CompleteInterviewCommand;
import com.jobpilot.application.interview.ports.InterviewRepository;
import com.jobpilot.application.interview.usecase.CompleteInterviewUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.interview.InterviewSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompleteInterviewService implements CompleteInterviewUseCase {

    private final InterviewRepository interviewRepository;

    public CompleteInterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Override
    public InterviewResponse execute(CompleteInterviewCommand command) {
        var sessionId = InterviewSessionId.from(command.sessionId());
        var session = interviewRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("InterviewSession", command.sessionId().toString()));
        session.complete(command.rating(), command.feedback());
        interviewRepository.save(session);
        return InterviewResponse.from(session);
    }
}
