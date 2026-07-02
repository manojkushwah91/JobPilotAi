package com.jobpilot.application.resume.service;

import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.resume.dto.UpdateResumeCommand;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.application.resume.usecase.UpdateResumeUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.resume.Resume;
import com.jobpilot.domain.resume.ResumeId;
import com.jobpilot.domain.resume.ResumeSectionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UpdateResumeService implements UpdateResumeUseCase {

    private final ResumeRepository resumeRepository;

    public UpdateResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Override
    public ResumeResponse execute(UpdateResumeCommand command) {
        var resumeId = ResumeId.from(UUID.fromString(command.resumeId()));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new NotFoundException("Resume", command.resumeId()));

        if (resume.isDeleted()) {
            throw new NotFoundException("Resume", command.resumeId());
        }

        resume.updateTitle(command.title());

        var existingSections = resume.sections();
        var incomingSections = command.sections();

        for (int i = existingSections.size() - 1; i >= 0; i--) {
            resume.removeSection(i);
        }

        for (var sectionDto : incomingSections) {
            var type = ResumeSectionType.valueOf(sectionDto.type());
            resume.addSection(type, sectionDto.title(), sectionDto.content(), sectionDto.sortOrder());
        }

        resumeRepository.save(resume);
        return CreateResumeService.toResponse(resume);
    }
}
