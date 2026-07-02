package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.ports.ScheduledTaskRepository;
import com.jobpilot.domain.automation.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class AutomationRunner {

    private static final Logger log = LoggerFactory.getLogger(AutomationRunner.class);

    private final ScheduledTaskRepository scheduledTaskRepository;

    public AutomationRunner(ScheduledTaskRepository scheduledTaskRepository) {
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processPendingTasks() {
        var before = Instant.now();
        var page = scheduledTaskRepository.findPendingTasks(before, PageRequest.of(0, 100));
        for (var task : page.getContent()) {
            try {
                executeTask(task);
                task.markCompleted();
            } catch (Exception e) {
                log.error("Failed to execute task {}: {}", task.taskId().value(), e.getMessage());
                task.markFailed();
            }
            scheduledTaskRepository.save(task);
        }
    }

    private void executeTask(ScheduledTask task) {
        log.info("Executing task {} of type {}", task.taskId().value(), task.taskType());
    }
}
