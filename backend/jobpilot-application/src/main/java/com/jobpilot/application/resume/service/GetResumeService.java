package com.jobpilot.application.resume.service;

import com.jobpilot.application.resume.dto.GetResumeCommand;
import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.application.resume.usecase.GetResumeUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.resume.ResumeId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetResumeService implements GetResumeUseCase {

    private final ResumeRepository resumeRepository;

    public GetResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Override
    public ResumeResponse execute(GetResumeCommand command) {
        var resumeId = ResumeId.from(UUID.fromString(command.resumeId()));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new NotFoundException("Resume", command.resumeId()));

        if (resume.isDeleted()) {
            throw new NotFoundException("Resume", command.resumeId());
        }

        return CreateResumeService.toResponse(resume);
    }
}
