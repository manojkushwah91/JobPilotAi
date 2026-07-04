package com.jobpilot.application.automation.service;

import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.ports.PlaywrightAutomationPort;
import com.jobpilot.application.automation.ports.ScheduledTaskRepository;
import com.jobpilot.application.automation.workflow.AutoApplyWorkflow;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.domain.application.ApplicationId;
import com.jobpilot.domain.automation.AutomationStatus;
import com.jobpilot.domain.automation.ScheduledTask;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.job.JobId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class AutomationRunner {

    private static final Logger log = LoggerFactory.getLogger(AutomationRunner.class);

    private final ScheduledTaskRepository scheduledTaskRepository;
    private final AutomationRepository automationRepository;
    private final PlaywrightAutomationPort playwright;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public AutomationRunner(ScheduledTaskRepository scheduledTaskRepository,
                            AutomationRepository automationRepository,
                            PlaywrightAutomationPort playwright,
                            UserRepository userRepository,
                            ApplicationRepository applicationRepository,
                            JobRepository jobRepository) {
        this.scheduledTaskRepository = scheduledTaskRepository;
        this.automationRepository = automationRepository;
        this.playwright = playwright;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
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

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processQueuedSessions() {
        var sessions = automationRepository.findByStatus(
            AutomationStatus.QUEUED, PageRequest.of(0, 10));
        for (var session : sessions) {
            try {
                session.updateProgress(5, "Starting automation");
                var user = userRepository.findById(UserId.from(session.userId())).orElse(null);
                if (user == null) {
                    session.fail("User not found");
                    automationRepository.save(session);
                    continue;
                }

                var app = applicationRepository.findById(
                    ApplicationId.from(session.applicationId())).orElse(null);
                if (app == null) {
                    session.fail("Application not found");
                    automationRepository.save(session);
                    continue;
                }

                var job = jobRepository.findById(app.jobListingId()).orElse(null);
                var appUrl = job != null && job.applicationUrl() != null && !job.applicationUrl().isBlank()
                    ? job.applicationUrl() : "";

                session.updateProgress(10, "Starting browser automation");
                var workflow = new AutoApplyWorkflow(playwright, appUrl, user);
                workflow.execute(session);
                automationRepository.save(session);
                log.info("Completed automation session {} for {}", session.sessionId().value(), appUrl);
            } catch (Exception e) {
                log.error("Automation session {} failed: {}", session.sessionId().value(), e.getMessage());
                session.fail("Automation failed: " + e.getMessage());
                automationRepository.save(session);
            }
        }
    }

    private void executeTask(ScheduledTask task) {
        log.info("Executing task {} of type {}", task.taskId().value(), task.taskType());
    }
}