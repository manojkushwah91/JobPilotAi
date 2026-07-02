package com.jobpilot.application.resume.ports;

import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.resume.Resume;
import com.jobpilot.domain.resume.ResumeId;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository {
    Resume save(Resume resume);
    Optional<Resume> findById(ResumeId id);
    List<Resume> findByUserId(UserId userId);
    Optional<Resume> findDefaultByUserId(UserId userId);
    void delete(Resume resume);
}
