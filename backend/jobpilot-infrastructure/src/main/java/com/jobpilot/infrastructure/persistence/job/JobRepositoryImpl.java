package com.jobpilot.infrastructure.persistence.job;

import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JobRepositoryImpl implements JobRepository {

    private final JobListingJpaRepository jpaRepository;

    public JobRepositoryImpl(JobListingJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    public JobListing save(JobListing job) {
        return jpaRepository.save(JobListingEntity.fromDomain(job)).toDomain();
    }

    @Override
    public Optional<JobListing> findById(JobId id) {
        return jpaRepository.findById(id.value()).map(JobListingEntity::toDomain);
    }

    @Override
    public Page<JobListing> findAllActive(Pageable pageable) {
        return jpaRepository.findByIsActiveTrueOrderByPostedAtDesc(pageable).map(JobListingEntity::toDomain);
    }

    @Override
    public Page<JobListing> search(String query, Pageable pageable) {
        return jpaRepository.search(query, pageable).map(JobListingEntity::toDomain);
    }

    @Override
    public Page<JobListing> searchFiltered(String query, List<String> skills, String employmentType,
                                            String experienceLevel, String industry, String location,
                                            Integer salaryMin, Integer salaryMax, String postedWithin,
                                            Pageable pageable) {
        var spec = JobSearchSpecification.withFilters(query, skills, employmentType,
            experienceLevel, industry, location, salaryMin, salaryMax, postedWithin);
        return jpaRepository.findAll(spec, pageable).map(JobListingEntity::toDomain);
    }

    @Override
    public Optional<JobListing> findByApplicationUrl(String applicationUrl) {
        if (applicationUrl == null || applicationUrl.isBlank()) return Optional.empty();
        return jpaRepository.findByApplicationUrl(applicationUrl).map(JobListingEntity::toDomain);
    }

    @Override
    public void delete(JobListing job) {
        jpaRepository.deleteById(job.jobId().value());
    }
}
