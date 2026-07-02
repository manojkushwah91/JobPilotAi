package com.jobpilot.infrastructure.persistence.company;

import com.jobpilot.application.company.ports.CompanyRepository;
import com.jobpilot.domain.company.CompanyId;
import com.jobpilot.domain.company.CompanyProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyProfileJpaRepository jpaRepository;

    public CompanyRepositoryImpl(CompanyProfileJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    public CompanyProfile save(CompanyProfile company) {
        return jpaRepository.save(CompanyProfileEntity.fromDomain(company)).toDomain();
    }

    @Override
    public Optional<CompanyProfile> findById(CompanyId id) {
        return jpaRepository.findById(id.value()).map(CompanyProfileEntity::toDomain);
    }

    @Override
    public Optional<CompanyProfile> findByName(String name) {
        return jpaRepository.findByName(name).map(CompanyProfileEntity::toDomain);
    }

    @Override
    public Page<CompanyProfile> search(String query, Pageable pageable) {
        return jpaRepository.search(query, pageable).map(CompanyProfileEntity::toDomain);
    }

    @Override
    public Page<CompanyProfile> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(CompanyProfileEntity::toDomain);
    }
}
