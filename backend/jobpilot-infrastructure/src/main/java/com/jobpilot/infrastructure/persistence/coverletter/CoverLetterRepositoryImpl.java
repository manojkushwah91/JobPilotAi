package com.jobpilot.infrastructure.persistence.coverletter;

import com.jobpilot.application.coverletter.ports.CoverLetterRepository;
import com.jobpilot.domain.coverletter.CoverLetter;
import com.jobpilot.domain.coverletter.CoverLetterId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CoverLetterRepositoryImpl implements CoverLetterRepository {

    private final CoverLetterJpaRepository jpaRepository;

    public CoverLetterRepositoryImpl(CoverLetterJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CoverLetter save(CoverLetter coverLetter) {
        var entity = CoverLetterEntity.fromDomain(coverLetter);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<CoverLetter> findById(CoverLetterId id) {
        return jpaRepository.findById(id.value())
            .map(CoverLetterEntity::toDomain);
    }

    @Override
    public List<CoverLetter> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(CoverLetterEntity::toDomain)
            .toList();
    }

    @Override
    public void delete(CoverLetter coverLetter) {
        jpaRepository.deleteById(coverLetter.coverLetterId().value());
    }
}
