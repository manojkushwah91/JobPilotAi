package com.jobpilot.application.interview.service;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.interview.dto.GetInterviewCommand;
import com.jobpilot.application.interview.ports.InterviewRepository;
import com.jobpilot.application.interview.usecase.GetInterviewUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.interview.InterviewSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetInterviewService implements GetInterviewUseCase {

    private final InterviewRepository interviewRepository;

    public GetInterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Override
    public InterviewResponse execute(GetInterviewCommand command) {
        var sessionId = InterviewSessionId.from(UUID.fromString(command.sessionId()));
        var session = interviewRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("InterviewSession", command.sessionId()));
        return InterviewResponse.from(session);
    }
}
