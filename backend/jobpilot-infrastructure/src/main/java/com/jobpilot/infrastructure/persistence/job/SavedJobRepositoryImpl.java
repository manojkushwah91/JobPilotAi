package com.jobpilot.infrastructure.persistence.job;

import com.jobpilot.application.job.ports.SavedJobRepository;
import com.jobpilot.domain.job.JobId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SavedJobRepositoryImpl implements SavedJobRepository {

    private final SavedJobJpaRepository jpaRepository;

    public SavedJobRepositoryImpl(SavedJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(UUID userId, JobId jobId, String notes) {
        jpaRepository.save(new SavedJobEntity(userId, jobId.value(), notes));
    }

    @Override
    public void delete(UUID userId, JobId jobId) {
        jpaRepository.deleteByUserIdAndJobListingId(userId, jobId.value());
    }

    @Override
    public boolean isSaved(UUID userId, JobId jobId) {
        return jpaRepository.existsByUserIdAndJobListingId(userId, jobId.value());
    }

    @Override
    public List<JobId> findAllByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
            .map(e -> JobId.from(e.getJobListingId()))
            .toList();
    }

    @Override
    public Optional<String> getNotes(UUID userId, JobId jobId) {
        var id = new SavedJobId(userId, jobId.value());
        return jpaRepository.findById(id).map(SavedJobEntity::getNotes);
    }
}
