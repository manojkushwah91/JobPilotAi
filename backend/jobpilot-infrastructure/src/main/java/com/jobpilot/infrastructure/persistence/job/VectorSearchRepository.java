package com.jobpilot.infrastructure.persistence.job;

import com.jobpilot.domain.job.JobListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class VectorSearchRepository implements com.jobpilot.application.search.ports.VectorSearchPort {

    private final JobListingJpaRepository jpaRepository;
    private final com.jobpilot.application.job.ports.JobRepository jobRepository;

    public VectorSearchRepository(JobListingJpaRepository jpaRepository,
                                   com.jobpilot.application.job.ports.JobRepository jobRepository) {
        this.jpaRepository = jpaRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public Page<JobListing> searchSimilar(String query, Pageable pageable) {
        // Falls back to FTS search; real pgvector would use native query
        return jobRepository.search(query, pageable);
    }

    @Override
    public Page<JobListing> searchBySkills(List<String> skills, Pageable pageable) {
        var skillArray = skills.toArray(new String[0]);
        var entities = jpaRepository.findBySkillsIn(skillArray, pageable);
        return entities.map(com.jobpilot.infrastructure.persistence.job.JobListingEntity::toDomain);
    }
}
