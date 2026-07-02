package com.jobpilot.application.interview.ports;

import com.jobpilot.domain.interview.InterviewSession;
import com.jobpilot.domain.interview.InterviewSessionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository {
    InterviewSession save(InterviewSession session);
    Optional<InterviewSession> findById(InterviewSessionId id);
    Page<InterviewSession> findByUserId(UUID userId, Pageable pageable);
    Page<InterviewSession> findByCompanyId(UUID companyId, Pageable pageable);
    void delete(InterviewSessionId id);
}
