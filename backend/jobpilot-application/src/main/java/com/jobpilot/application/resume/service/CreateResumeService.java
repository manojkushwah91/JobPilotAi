package com.jobpilot.application.resume.service;

import com.jobpilot.application.resume.dto.CreateResumeCommand;
import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.resume.dto.SectionDto;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.application.resume.usecase.CreateResumeUseCase;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.resume.Resume;
import com.jobpilot.domain.resume.ResumeId;
import com.jobpilot.domain.resume.ResumeSectionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CreateResumeService implements CreateResumeUseCase {

    private final ResumeRepository resumeRepository;

    public CreateResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Override
    public ResumeResponse execute(CreateResumeCommand command) {
        var resumeId = ResumeId.generate();
        var userId = UserId.from(UUID.fromString(command.userId()));
        var resume = Resume.create(resumeId, userId, command.title());

        for (var sectionDto : command.sections()) {
            var type = ResumeSectionType.valueOf(sectionDto.type());
            resume.addSection(type, sectionDto.title(), sectionDto.content(), sectionDto.sortOrder());
        }

        resumeRepository.save(resume);
        return toResponse(resume);
    }

    public static ResumeResponse toResponse(Resume resume) {
        var sections = resume.sections().stream()
            .map(s -> new SectionDto(s.id().toString(), s.type().name(), s.title(), s.content(), s.sortOrder()))
            .toList();

        return new ResumeResponse(
            resume.resumeId().value().toString(),
            resume.userId().value().toString(),
            resume.title(),
            resume.atsScore(),
            resume.atsScoreData(),
            resume.resumeVersion(),
            resume.isDefault(),
            sections,
            resume.createdAt(),
            resume.updatedAt()
        );
    }
}
