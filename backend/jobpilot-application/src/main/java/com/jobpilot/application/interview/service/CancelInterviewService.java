package com.jobpilot.application.interview.service;

import com.jobpilot.application.interview.dto.CancelInterviewCommand;
import com.jobpilot.application.interview.ports.InterviewRepository;
import com.jobpilot.application.interview.usecase.CancelInterviewUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.interview.InterviewSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CancelInterviewService implements CancelInterviewUseCase {

    private final InterviewRepository interviewRepository;

    public CancelInterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Override
    public Void execute(CancelInterviewCommand command) {
        var sessionId = InterviewSessionId.from(UUID.fromString(command.sessionId()));
        var session = interviewRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("InterviewSession", command.sessionId()));
        session.cancel(command.reason());
        interviewRepository.save(session);
        return null;
    }
}
