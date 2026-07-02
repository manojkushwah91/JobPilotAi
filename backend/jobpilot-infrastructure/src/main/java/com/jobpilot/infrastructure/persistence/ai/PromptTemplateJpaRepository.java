package com.jobpilot.infrastructure.persistence.ai;

import com.jobpilot.domain.ai.PromptTemplate;
import com.jobpilot.domain.ai.PromptTemplateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PromptTemplateJpaRepository extends JpaRepository<PromptTemplateEntity, UUID> {
    @Query("SELECT p FROM PromptTemplateEntity p WHERE p.useCase = ?1 AND p.active = true")
    Optional<PromptTemplateEntity> findActiveByUseCase(String useCase);
}
