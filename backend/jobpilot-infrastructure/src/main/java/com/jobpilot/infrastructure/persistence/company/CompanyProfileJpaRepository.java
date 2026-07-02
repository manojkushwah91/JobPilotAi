package com.jobpilot.infrastructure.persistence.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompanyProfileJpaRepository extends JpaRepository<CompanyProfileEntity, UUID> {
    Optional<CompanyProfileEntity> findByName(String name);

    @Query("SELECT c FROM CompanyProfileEntity c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<CompanyProfileEntity> search(@Param("query") String query, Pageable pageable);
}
