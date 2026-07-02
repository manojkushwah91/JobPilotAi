package com.jobpilot.infrastructure.persistence.questionbank;

import com.jobpilot.application.questionbank.ports.QuestionBankRepository;
import com.jobpilot.domain.questionbank.QuestionBankEntry;
import com.jobpilot.domain.questionbank.QuestionBankId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class QuestionBankRepositoryImpl implements QuestionBankRepository {

    private final QuestionBankJpaRepository jpaRepository;

    public QuestionBankRepositoryImpl(QuestionBankJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Page<QuestionBankEntry> search(String query, String category, String type, Integer difficulty, Pageable pageable) {
        return jpaRepository.search(query, category, type, difficulty, pageable)
            .map(QuestionBankEntryEntity::toDomain);
    }

    @Override
    public Optional<QuestionBankEntry> findById(QuestionBankId id) {
        return jpaRepository.findById(id.value()).map(QuestionBankEntryEntity::toDomain);
    }

    @Override
    public QuestionBankEntry save(QuestionBankEntry entry) {
        return jpaRepository.save(QuestionBankEntryEntity.fromDomain(entry)).toDomain();
    }
}
