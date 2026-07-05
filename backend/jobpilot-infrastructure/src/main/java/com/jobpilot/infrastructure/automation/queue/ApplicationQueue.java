package com.jobpilot.infrastructure.automation.queue;

import com.jobpilot.infrastructure.persistence.automation.ApplicationResultJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApplicationQueue {

    private static final Logger log = LoggerFactory.getLogger(ApplicationQueue.class);

    private final ConcurrentLinkedQueue<JobApplicationRequest> queue = new ConcurrentLinkedQueue<>();
    private final Set<String> processedUrls = ConcurrentHashMap.newKeySet();
    private final ApplicationResultJpaRepository resultRepository;

    public ApplicationQueue(ApplicationResultJpaRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public boolean enqueue(JobApplicationRequest request) {
        if (processedUrls.contains(request.jobUrl())) {
            log.debug("Skipping duplicate application: {}", request.jobUrl());
            return false;
        }

        var existing = resultRepository.findByOutcome("SUBMITTED");
        var alreadyApplied = existing.stream()
            .anyMatch(r -> r.getJobUrl().equals(request.jobUrl()));

        if (alreadyApplied) {
            log.debug("Already applied to: {}", request.jobUrl());
            processedUrls.add(request.jobUrl());
            return false;
        }

        queue.offer(request);
        log.info("Queued application for: {}", request.jobUrl());
        return true;
    }

    public Optional<JobApplicationRequest> dequeue() {
        var request = queue.poll();
        if (request != null) {
            processedUrls.add(request.jobUrl());
        }
        return Optional.ofNullable(request);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void clear() {
        queue.clear();
    }

    public Set<String> getProcessedUrls() {
        return Collections.unmodifiableSet(processedUrls);
    }

    public void loadProcessedUrls() {
        var submitted = resultRepository.findByOutcome("SUBMITTED");
        submitted.forEach(r -> processedUrls.add(r.getJobUrl()));
        var failed = resultRepository.findByOutcome("FAILED");
        failed.forEach(r -> processedUrls.add(r.getJobUrl()));
        log.info("Loaded {} processed URLs", processedUrls.size());
    }

    public record JobApplicationRequest(
        String jobUrl,
        String boardName,
        String jobTitle,
        String companyName,
        Map<String, String> userProfile
    ) {}
}
