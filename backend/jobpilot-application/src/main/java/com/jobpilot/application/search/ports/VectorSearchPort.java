package com.jobpilot.application.search.ports;

import com.jobpilot.domain.job.JobListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VectorSearchPort {
    Page<JobListing> searchSimilar(String query, Pageable pageable);
    Page<JobListing> searchBySkills(List<String> skills, Pageable pageable);
}
