package com.jobpilot.infrastructure.persistence.interview;

import com.jobpilot.application.interview.ports.InterviewRepository;
import com.jobpilot.domain.interview.InterviewSession;
import com.jobpilot.domain.interview.InterviewSessionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InterviewRepositoryImpl implements InterviewRepository {

    private final InterviewSessionJpaRepository jpaRepository;

    public InterviewRepositoryImpl(InterviewSessionJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    public InterviewSession save(InterviewSession session) {
        return jpaRepository.save(InterviewSessionEntity.fromDomain(session)).toDomain();
    }

    @Override
    public Optional<InterviewSession> findById(InterviewSessionId id) {
        return jpaRepository.findById(id.value()).map(InterviewSessionEntity::toDomain);
    }

    @Override
    public Page<InterviewSession> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(InterviewSessionEntity::toDomain);
    }

    @Override
    public Page<InterviewSession> findByCompanyId(UUID companyId, Pageable pageable) {
        return jpaRepository.findByCompanyId(companyId, pageable).map(InterviewSessionEntity::toDomain);
    }

    @Override
    public void delete(InterviewSessionId id) {
        jpaRepository.deleteById(id.value());
    }
}
