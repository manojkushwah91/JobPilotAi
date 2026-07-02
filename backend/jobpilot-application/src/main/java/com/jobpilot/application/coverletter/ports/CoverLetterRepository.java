package com.jobpilot.application.coverletter.ports;

import com.jobpilot.domain.coverletter.CoverLetter;
import com.jobpilot.domain.coverletter.CoverLetterId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoverLetterRepository {
    CoverLetter save(CoverLetter coverLetter);
    Optional<CoverLetter> findById(CoverLetterId id);
    List<CoverLetter> findByUserId(UUID userId);
    void delete(CoverLetter coverLetter);
}
