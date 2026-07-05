package com.jobpilot.infrastructure.persistence.resume;

import com.jobpilot.application.resume.ports.ResumeVersionRepository;
import com.jobpilot.domain.resume.ResumeVersion;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ResumeVersionRepositoryImpl implements ResumeVersionRepository {

    private final ResumeVersionJpaRepository jpaRepository;

    public ResumeVersionRepositoryImpl(ResumeVersionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ResumeVersion save(ResumeVersion version) {
        var entity = ResumeVersionJpaEntity.fromDomain(version);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<ResumeVersion> findByResumeIdAndJobUrl(UUID resumeId, String jobUrl) {
        return jpaRepository.findByResumeIdAndJobUrl(resumeId.toString(), jobUrl)
            .map(ResumeVersionJpaEntity::toDomain);
    }

    @Override
    public List<ResumeVersion> findByResumeId(UUID resumeId) {
        return jpaRepository.findByResumeIdOrderByCreatedAtDesc(resumeId.toString())
            .stream()
            .map(ResumeVersionJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<ResumeVersion> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.toString())
            .stream()
            .map(ResumeVersionJpaEntity::toDomain)
            .toList();
    }
}
