package com.jobpilot.infrastructure.persistence.ai;

import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import com.jobpilot.domain.ai.PromptTemplate;
import com.jobpilot.domain.ai.PromptTemplateId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PromptTemplateRepositoryImpl implements PromptTemplateRepository {

    private final PromptTemplateJpaRepository jpaRepository;

    public PromptTemplateRepositoryImpl(PromptTemplateJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    public PromptTemplate save(PromptTemplate template) {
        return jpaRepository.save(PromptTemplateEntity.fromDomain(template)).toDomain();
    }

    @Override
    public Optional<PromptTemplate> findById(PromptTemplateId id) {
        return jpaRepository.findById(id.value()).map(PromptTemplateEntity::toDomain);
    }

    @Override
    public Optional<PromptTemplate> findActiveByUseCase(String useCase) {
        return jpaRepository.findActiveByUseCase(useCase).map(PromptTemplateEntity::toDomain);
    }
}
