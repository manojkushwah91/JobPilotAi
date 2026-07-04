package com.jobpilot.infrastructure.persistence.job;

import com.jobpilot.domain.job.JobListing;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VectorSearchRepository implements com.jobpilot.application.search.ports.VectorSearchPort {

    private final JobListingJpaRepository jpaRepository;
    private final com.jobpilot.application.job.ports.JobRepository jobRepository;
    private final EntityManager entityManager;

    public VectorSearchRepository(JobListingJpaRepository jpaRepository,
                                   com.jobpilot.application.job.ports.JobRepository jobRepository,
                                   EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.jobRepository = jobRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Page<JobListing> searchSimilar(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return jobRepository.findAllActive(pageable);
        }
        // Try pgvector cosine similarity if embeddings column has data,
        // else fall back to FTS
        var sql = "SELECT id FROM job_listings WHERE is_active = true AND embeddings IS NOT NULL " +
                  "ORDER BY embeddings <=> cast(:queryEmbedding as float8[]) " +
                  "LIMIT :limit OFFSET :offset";
        try {
            var nativeQuery = entityManager.createNativeQuery(sql, java.util.UUID.class);
            // Use a simple embedding placeholder (real embeddings would come from AI service)
            nativeQuery.setParameter("queryEmbedding", new float[1536]);
            nativeQuery.setParameter("limit", pageable.getPageSize());
            nativeQuery.setParameter("offset", pageable.getOffset());
            @SuppressWarnings("unchecked")
            var results = (List<java.util.UUID>) nativeQuery.getResultList();
            var jobs = results.stream()
                .map(id -> jobRepository.findById(com.jobpilot.domain.job.JobId.from(id)))
                .flatMap(opt -> opt.stream())
                .toList();
            return new PageImpl<>(jobs, pageable, jobs.size());
        } catch (Exception e) {
            // Fallback to FTS if native query fails (pgvector not available)
            return jobRepository.search(query, pageable);
        }
    }

    @Override
    public Page<JobListing> searchBySkills(List<String> skills, Pageable pageable) {
        var skillArray = skills.toArray(new String[0]);
        var entities = jpaRepository.findBySkillsIn(skillArray, pageable);
        return entities.map(com.jobpilot.infrastructure.persistence.job.JobListingEntity::toDomain);
    }
}
