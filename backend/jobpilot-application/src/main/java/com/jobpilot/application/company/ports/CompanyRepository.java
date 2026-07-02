package com.jobpilot.application.company.ports;

import com.jobpilot.domain.company.CompanyId;
import com.jobpilot.domain.company.CompanyProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CompanyRepository {
    CompanyProfile save(CompanyProfile company);
    Optional<CompanyProfile> findById(CompanyId id);
    Optional<CompanyProfile> findByName(String name);
    Page<CompanyProfile> search(String query, Pageable pageable);
    Page<CompanyProfile> findAll(Pageable pageable);
}
