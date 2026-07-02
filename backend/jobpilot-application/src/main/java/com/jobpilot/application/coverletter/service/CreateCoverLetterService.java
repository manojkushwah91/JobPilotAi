package com.jobpilot.application.coverletter.service;

import com.jobpilot.application.coverletter.dto.CoverLetterResponse;
import com.jobpilot.application.coverletter.dto.CreateCoverLetterCommand;
import com.jobpilot.application.coverletter.ports.CoverLetterRepository;
import com.jobpilot.application.coverletter.usecase.CreateCoverLetterUseCase;
import com.jobpilot.domain.coverletter.CoverLetter;
import com.jobpilot.domain.coverletter.CoverLetterId;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateCoverLetterService implements CreateCoverLetterUseCase {

    private final CoverLetterRepository coverLetterRepository;

    public CreateCoverLetterService(CoverLetterRepository coverLetterRepository) {
        this.coverLetterRepository = coverLetterRepository;
    }

    @Override
    public CoverLetterResponse execute(CreateCoverLetterCommand command) {
        var coverLetterId = CoverLetterId.generate();
        var userId = UserId.from(command.userId());
        var coverLetter = CoverLetter.create(coverLetterId, userId, command.title());
        coverLetter.updateContent(command.content());
        coverLetter.updateCompanyName(command.companyName());
        if (command.tone() != null) {
            coverLetter.updateTone(command.tone());
        }

        coverLetterRepository.save(coverLetter);
        return CoverLetterResponse.from(coverLetter);
    }
}
