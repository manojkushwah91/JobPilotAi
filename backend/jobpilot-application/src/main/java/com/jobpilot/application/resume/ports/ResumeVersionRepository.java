package com.jobpilot.application.resume.ports;

import com.jobpilot.domain.resume.ResumeVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeVersionRepository {

    ResumeVersion save(ResumeVersion version);

    Optional<ResumeVersion> findByResumeIdAndJobUrl(UUID resumeId, String jobUrl);

    List<ResumeVersion> findByResumeId(UUID resumeId);

    List<ResumeVersion> findByUserId(UUID userId);
}
