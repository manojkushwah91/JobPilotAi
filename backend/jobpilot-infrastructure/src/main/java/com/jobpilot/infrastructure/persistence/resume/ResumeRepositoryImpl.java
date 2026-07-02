package com.jobpilot.infrastructure.persistence.resume;

import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.resume.Resume;
import com.jobpilot.domain.resume.ResumeId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ResumeRepositoryImpl implements ResumeRepository {

    private final ResumeJpaRepository jpaRepository;

    public ResumeRepositoryImpl(ResumeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Resume save(Resume resume) {
        var entity = ResumeEntity.fromDomain(resume);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Resume> findById(ResumeId id) {
        return jpaRepository.findById(id.value())
            .map(ResumeEntity::toDomain);
    }

    @Override
    public List<Resume> findByUserId(UserId userId) {
        return jpaRepository.findByUserIdOrderByUpdatedAtDesc(userId.value())
            .stream()
            .map(ResumeEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Resume> findDefaultByUserId(UserId userId) {
        return jpaRepository.findByUserIdAndIsDefaultTrue(userId.value())
            .map(ResumeEntity::toDomain);
    }

    @Override
    public void delete(Resume resume) {
        jpaRepository.deleteById(resume.resumeId().value());
    }
}
