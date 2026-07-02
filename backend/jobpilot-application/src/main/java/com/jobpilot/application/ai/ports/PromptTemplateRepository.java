package com.jobpilot.application.ai.ports;

import com.jobpilot.domain.ai.PromptTemplate;
import com.jobpilot.domain.ai.PromptTemplateId;

import java.util.Optional;

public interface PromptTemplateRepository {
    PromptTemplate save(PromptTemplate template);
    Optional<PromptTemplate> findById(PromptTemplateId id);
    Optional<PromptTemplate> findActiveByUseCase(String useCase);
}
