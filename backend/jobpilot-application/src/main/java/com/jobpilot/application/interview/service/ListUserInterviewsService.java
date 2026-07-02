package com.jobpilot.application.interview.service;

import com.jobpilot.application.interview.dto.InterviewResponse;
import com.jobpilot.application.interview.ports.InterviewRepository;
import com.jobpilot.application.interview.usecase.ListUserInterviewsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListUserInterviewsService implements ListUserInterviewsUseCase {

    private final InterviewRepository interviewRepository;

    public ListUserInterviewsService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Override
    public List<InterviewResponse> execute(UUID userId) {
        return interviewRepository.findByUserId(userId, org.springframework.data.domain.Pageable.unpaged())
            .stream().map(InterviewResponse::from).collect(Collectors.toList());
    }
}
