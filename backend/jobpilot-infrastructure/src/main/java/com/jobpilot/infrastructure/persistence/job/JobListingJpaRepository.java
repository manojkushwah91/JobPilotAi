package com.jobpilot.infrastructure.persistence.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JobListingJpaRepository extends JpaRepository<JobListingEntity, UUID> {

    Page<JobListingEntity> findByIsActiveTrueOrderByPostedAtDesc(Pageable pageable);

    @Query(value = "SELECT j FROM JobListingEntity j WHERE j.isActive = true AND (" +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.industry) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<JobListingEntity> search(@Param("query") String query, Pageable pageable);

    Page<JobListingEntity> findBySkillsIn(String[] skills, Pageable pageable);
}
