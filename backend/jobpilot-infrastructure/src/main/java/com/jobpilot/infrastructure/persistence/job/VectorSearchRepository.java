package com.jobpilot.infrastructure.persistence.job;

import com.jobpilot.domain.job.JobListing;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Repository
public class VectorSearchRepository implements com.jobpilot.application.search.ports.VectorSearchPort {

    private final JobListingJpaRepository jpaRepository;
    private final com.jobpilot.application.job.ports.JobRepository jobRepository;
    private final EntityManager entityManager;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ai.ollama.embed-model:nomic-embed-text}")
    private String embedModel;

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
        var sql = "SELECT id FROM job_listings WHERE is_active = true AND embeddings IS NOT NULL " +
                  "ORDER BY embeddings <=> cast(:queryEmbedding as float8[]) " +
                  "LIMIT :limit OFFSET :offset";
        try {
            var embedding = getEmbedding(query);
            var nativeQuery = entityManager.createNativeQuery(sql, java.util.UUID.class);
            nativeQuery.setParameter("queryEmbedding", embedding);
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
            return jobRepository.search(query, pageable);
        }
    }

    @Override
    public Page<JobListing> searchBySkills(List<String> skills, Pageable pageable) {
        var skillArray = skills.toArray(new String[0]);
        var entities = jpaRepository.findBySkillsIn(skillArray, pageable);
        return entities.map(com.jobpilot.infrastructure.persistence.job.JobListingEntity::toDomain);
    }

    private float[] getEmbedding(String text) {
        try {
            var rt = getRestTemplate();
            var body = Map.of("model", embedModel, "prompt", text);
            var response = rt.postForObject(ollamaBaseUrl + "/api/embeddings", body, com.fasterxml.jackson.databind.JsonNode.class);
            if (response != null && response.has("embedding")) {
                var arr = response.get("embedding");
                var result = new float[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    result[i] = arr.get(i).floatValue();
                }
                return result;
            }
        } catch (Exception e) {
            // Ollama unavailable - caller will catch and fall back to FTS
        }
        throw new RuntimeException("Unable to generate embedding from Ollama");
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate != null) {
            return restTemplate;
        }
        return new RestTemplate();
    }
}
