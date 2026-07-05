package com.jobpilot.infrastructure.persistence.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeVersionJpaRepository extends JpaRepository<ResumeVersionJpaEntity, String> {

    List<ResumeVersionJpaEntity> findByResumeIdOrderByCreatedAtDesc(String resumeId);

    List<ResumeVersionJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<ResumeVersionJpaEntity> findByResumeIdAndJobUrl(String resumeId, String jobUrl);
}
