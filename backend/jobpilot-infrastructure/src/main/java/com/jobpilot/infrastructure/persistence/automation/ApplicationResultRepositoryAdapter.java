package com.jobpilot.infrastructure.persistence.automation;

import com.jobpilot.domain.automation.ApplicationResult;
import com.jobpilot.domain.automation.ApplicationOutcome;
import com.jobpilot.domain.automation.ResultId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ApplicationResultRepositoryAdapter {

    private final ApplicationResultJpaRepository repository;

    public ApplicationResultRepositoryAdapter(ApplicationResultJpaRepository repository) {
        this.repository = repository;
    }

    public ApplicationResult save(ApplicationResult result) {
        var entity = toEntity(result);
        repository.save(entity);
        return result;
    }

    public Optional<ApplicationResult> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    public List<ApplicationResult> findBySessionId(String sessionId) {
        return repository.findBySessionId(sessionId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public List<ApplicationResult> findByOutcome(ApplicationOutcome outcome) {
        return repository.findByOutcome(outcome.name()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public int countSubmittedApplications() {
        return repository.countSubmittedApplications();
    }

    public int countFailedApplications() {
        return repository.countFailedApplications();
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private ApplicationResultJpaEntity toEntity(ApplicationResult result) {
        var entity = new ApplicationResultJpaEntity();
        entity.setId(result.resultId().value().toString());
        entity.setSessionId(result.sessionId().toString());
        entity.setJobUrl(result.jobUrl());
        entity.setOutcome(result.outcome().name());
        entity.setErrorMessage(result.errorMessage());
        return entity;
    }

    private ApplicationResult toDomain(ApplicationResultJpaEntity entity) {
        var resultId = ResultId.from(java.util.UUID.fromString(entity.getId()));
        var outcome = ApplicationOutcome.valueOf(entity.getOutcome());

        var result = ApplicationResult.create(null, null, null);
        result.setJobUrl(entity.getJobUrl());

        switch (outcome) {
            case SUBMITTED -> result.markSubmitted();
            case FAILED -> result.markFailed(entity.getErrorMessage());
            case REJECTED -> result.markRejected(entity.getErrorMessage());
            case REQUIRES_CAPTCHA -> result.markRequiresCaptcha();
            case REQUIRES_APPROVAL -> result.markRequiresApproval();
            default -> {}
        }

        return result;
    }
}
