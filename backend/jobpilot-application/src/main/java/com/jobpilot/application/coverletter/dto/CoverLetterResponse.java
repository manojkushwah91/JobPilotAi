package com.jobpilot.application.coverletter.dto;

import com.jobpilot.domain.coverletter.CoverLetter;

import java.time.Instant;

public record CoverLetterResponse(
    String id,
    String userId,
    String title,
    String companyName,
    String jobTitle,
    String content,
    String tone,
    String recipientName,
    boolean aiGenerated,
    Instant createdAt,
    Instant updatedAt
) {
    public static CoverLetterResponse from(CoverLetter coverLetter) {
        return new CoverLetterResponse(
            coverLetter.coverLetterId().value().toString(),
            coverLetter.userId().value().toString(),
            coverLetter.title(),
            coverLetter.companyName(),
            coverLetter.jobTitle(),
            coverLetter.content(),
            coverLetter.tone(),
            coverLetter.recipientName(),
            coverLetter.isAiGenerated(),
            coverLetter.createdAt(),
            coverLetter.updatedAt()
        );
    }
}
