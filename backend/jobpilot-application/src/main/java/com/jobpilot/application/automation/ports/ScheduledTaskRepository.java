package com.jobpilot.application.automation.ports;

import com.jobpilot.domain.automation.ScheduledTask;
import com.jobpilot.domain.automation.ScheduledTaskId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

public interface ScheduledTaskRepository {
    ScheduledTask save(ScheduledTask task);
    Page<ScheduledTask> findPendingTasks(Instant before, Pageable pageable);
    Optional<ScheduledTask> findById(ScheduledTaskId id);
    void delete(ScheduledTaskId id);
}
