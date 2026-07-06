package com.jobpilot.infrastructure.persistence.agent;

import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.CandidateProfile;
import com.jobpilot.domain.agent.CandidateProfileId;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CandidateProfileRepositoryAdapter implements CandidateProfileRepository {

    private final CandidateProfileJpaRepository jpaRepository;

    public CandidateProfileRepositoryAdapter(CandidateProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CandidateProfile save(CandidateProfile profile) {
        var entity = CandidateProfileJpaEntity.fromDomain(profile);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<CandidateProfile> findById(CandidateProfileId profileId) {
        return jpaRepository.findById(profileId.value()).map(CandidateProfileJpaEntity::toDomain);
    }

    @Override
    public Optional<CandidateProfile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(CandidateProfileJpaEntity::toDomain);
    }

    @Override
    public void delete(CandidateProfileId profileId) {
        jpaRepository.deleteById(profileId.value());
    }
}
