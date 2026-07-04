package com.jobpilot.infrastructure.persistence.automation;

import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.automation.AutomationSessionId;
import com.jobpilot.domain.automation.AutomationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AutomationRepositoryImpl implements AutomationRepository {

    private final AutomationSessionJpaRepository jpaRepository;

    public AutomationRepositoryImpl(AutomationSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AutomationSession save(AutomationSession session) {
        return jpaRepository.save(AutomationSessionEntity.fromDomain(session)).toDomain();
    }

    @Override
    public Optional<AutomationSession> findById(AutomationSessionId id) {
        return jpaRepository.findById(id.value()).map(AutomationSessionEntity::toDomain);
    }

    @Override
    public Page<AutomationSession> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(AutomationSessionEntity::toDomain);
    }

    @Override
    public List<AutomationSession> findByStatus(AutomationStatus status, Pageable pageable) {
        return jpaRepository.findByStatus(status.name(), pageable).stream()
            .map(AutomationSessionEntity::toDomain)
            .toList();
    }
}
