package com.jobpilot.application.job.ports;

import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface JobRepository {
    JobListing save(JobListing job);
    Optional<JobListing> findById(JobId id);
    Page<JobListing> findAllActive(Pageable pageable);
    Page<JobListing> search(String query, Pageable pageable);
    void delete(JobListing job);
}
