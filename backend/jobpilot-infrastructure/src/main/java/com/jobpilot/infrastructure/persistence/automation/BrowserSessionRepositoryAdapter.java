package com.jobpilot.infrastructure.persistence.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.SessionStatus;
import com.jobpilot.domain.automation.SessionId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BrowserSessionRepositoryAdapter {

    private final BrowserSessionJpaRepository repository;

    public BrowserSessionRepositoryAdapter(BrowserSessionJpaRepository repository) {
        this.repository = repository;
    }

    public BrowserSession save(BrowserSession session) {
        var entity = toEntity(session);
        repository.save(entity);
        return session;
    }

    public Optional<BrowserSession> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    public List<BrowserSession> findAll() {
        return repository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public List<BrowserSession> findByStatus(SessionStatus status) {
        return repository.findByStatus(status.name()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public List<BrowserSession> findActiveSessions() {
        return repository.findActiveSessions().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public int countActiveSessions() {
        return repository.countActiveSessions();
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    private BrowserSessionJpaEntity toEntity(BrowserSession session) {
        var entity = new BrowserSessionJpaEntity();
        entity.setId(session.sessionId().value().toString());
        entity.setBoardName(session.adapterName());
        entity.setStatus(session.status().name());
        entity.setCurrentUrl(session.currentPageUrl());
        entity.setRetryCount(session.errorsEncountered());
        entity.setErrorMessage(session.errorMessage());
        return entity;
    }

    private BrowserSession toDomain(BrowserSessionJpaEntity entity) {
        var sessionId = SessionId.fromString(entity.getId());
        return BrowserSession.create(null, null, entity.getBoardName());
    }
}
