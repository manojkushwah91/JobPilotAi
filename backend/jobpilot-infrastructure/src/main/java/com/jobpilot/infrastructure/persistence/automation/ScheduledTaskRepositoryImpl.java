package com.jobpilot.infrastructure.persistence.automation;

import com.jobpilot.application.automation.ports.ScheduledTaskRepository;
import com.jobpilot.domain.automation.ScheduledTask;
import com.jobpilot.domain.automation.ScheduledTaskId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class ScheduledTaskRepositoryImpl implements ScheduledTaskRepository {

    private final ScheduledTaskJpaRepository jpaRepository;

    public ScheduledTaskRepositoryImpl(ScheduledTaskJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ScheduledTask save(ScheduledTask task) {
        return jpaRepository.save(ScheduledTaskEntity.fromDomain(task)).toDomain();
    }

    @Override
    public Page<ScheduledTask> findPendingTasks(Instant before, Pageable pageable) {
        return jpaRepository.findByStatusAndScheduledAtBefore("PENDING", before, pageable)
            .map(ScheduledTaskEntity::toDomain);
    }

    @Override
    public Optional<ScheduledTask> findById(ScheduledTaskId id) {
        return jpaRepository.findById(id.value()).map(ScheduledTaskEntity::toDomain);
    }

    @Override
    public void delete(ScheduledTaskId id) {
        jpaRepository.deleteById(id.value());
    }
}
