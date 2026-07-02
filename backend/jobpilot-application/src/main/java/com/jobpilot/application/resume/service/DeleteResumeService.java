package com.jobpilot.application.resume.service;

import com.jobpilot.application.resume.dto.DeleteResumeCommand;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.application.resume.usecase.DeleteResumeUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.resume.ResumeId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteResumeService implements DeleteResumeUseCase {

    private final ResumeRepository resumeRepository;

    public DeleteResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Override
    public Void execute(DeleteResumeCommand command) {
        var resumeId = ResumeId.from(UUID.fromString(command.resumeId()));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new NotFoundException("Resume", command.resumeId()));

        resume.softDelete();
        resumeRepository.save(resume);
        return null;
    }
}
