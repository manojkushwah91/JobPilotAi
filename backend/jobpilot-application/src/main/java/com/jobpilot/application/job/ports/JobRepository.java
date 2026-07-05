package com.jobpilot.application.job.ports;

import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface JobRepository {
    JobListing save(JobListing job);
    Optional<JobListing> findById(JobId id);
    Page<JobListing> findAllActive(Pageable pageable);
    Page<JobListing> search(String query, Pageable pageable);
    Page<JobListing> searchFiltered(String query, List<String> skills, String employmentType,
                                     String experienceLevel, String industry, String location,
                                     Integer salaryMin, Integer salaryMax, String postedWithin,
                                     Pageable pageable);
    Optional<JobListing> findByApplicationUrl(String applicationUrl);
    void delete(JobListing job);
}
