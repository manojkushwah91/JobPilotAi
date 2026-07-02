package com.jobpilot.infrastructure.persistence.questionbank;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface QuestionBankJpaRepository extends JpaRepository<QuestionBankEntryEntity, UUID> {

    @Query("SELECT q FROM QuestionBankEntryEntity q WHERE " +
        "(:query IS NULL OR LOWER(q.question) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
        "(:category IS NULL OR q.category = :category) AND " +
        "(:type IS NULL OR q.type = :type) AND " +
        "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    Page<QuestionBankEntryEntity> search(@Param("query") String query, @Param("category") String category,
                                         @Param("type") String type, @Param("difficulty") Integer difficulty,
                                         Pageable pageable);
}
