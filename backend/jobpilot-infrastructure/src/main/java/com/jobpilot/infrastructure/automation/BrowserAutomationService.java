package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.JobBoardAdapter;
import com.jobpilot.infrastructure.automation.multitab.MultiTabManager;
import com.jobpilot.infrastructure.automation.persistence.CookiePersistenceManager;
import com.jobpilot.infrastructure.automation.progress.AutomationProgressTracker;
import com.jobpilot.infrastructure.automation.proxy.ProxyManager;
import com.jobpilot.infrastructure.automation.queue.ApplicationQueue;
import com.jobpilot.infrastructure.automation.queue.ApplicationQueue.JobApplicationRequest;
import com.jobpilot.infrastructure.automation.retry.SmartRetryManager;
import com.jobpilot.infrastructure.automation.stealth.StealthManager;
import com.jobpilot.infrastructure.persistence.automation.ApplicationResultJpaRepository;
import com.jobpilot.infrastructure.persistence.automation.ApplicationResultJpaEntity;
import com.jobpilot.domain.automation.ApplicationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BrowserAutomationService {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutomationService.class);

    private final BrowserAutomationFramework framework;
    private final ApplicationQueue applicationQueue;
    private final AutomationProgressTracker progressTracker;
    private final StealthManager stealthManager;
    private final MultiTabManager multiTabManager;
    private final SmartRetryManager smartRetryManager;
    private final CookiePersistenceManager cookiePersistenceManager;
    private final ApplicationResultJpaRepository resultRepository;
    private final ProxyManager proxyManager;
    private final Map<String, JobBoardAdapter> adapters = new ConcurrentHashMap<>();

    private volatile boolean running = false;
    private volatile String currentSessionId = null;

    @Value("${jobpilot.browser.headless:true}")
    private boolean defaultHeadless;

    public BrowserAutomationService(BrowserAutomationFramework framework,
                                     ApplicationQueue applicationQueue,
                                     AutomationProgressTracker progressTracker,
                                     StealthManager stealthManager,
                                     MultiTabManager multiTabManager,
                                     SmartRetryManager smartRetryManager,
                                     CookiePersistenceManager cookiePersistenceManager,
                                     ApplicationResultJpaRepository resultRepository,
                                     ProxyManager proxyManager,
                                     List<JobBoardAdapter> adapterList) {
        this.framework = framework;
        this.applicationQueue = applicationQueue;
        this.progressTracker = progressTracker;
        this.stealthManager = stealthManager;
        this.multiTabManager = multiTabManager;
        this.smartRetryManager = smartRetryManager;
        this.cookiePersistenceManager = cookiePersistenceManager;
        this.resultRepository = resultRepository;
        this.proxyManager = proxyManager;

        for (var adapter : adapterList) {
            adapters.put(adapter.name().toLowerCase(), adapter);
        }
    }

    @Async
    public void runAutomation(String boardName, Map<String, String> credentials,
                               UUID userId, UUID missionId) {
        runAutomation(boardName, credentials, userId, missionId, defaultHeadless);
    }

    @Async
    public void runAutomation(String boardName, Map<String, String> credentials,
                               UUID userId, UUID missionId, boolean headless) {
        log.info("Starting automation for board={} user={} headless={}", boardName, userId, headless);
        running = true;
        var browserManager = framework.getBrowserManager();

        try {
            var adapter = adapters.get(boardName.toLowerCase());
            if (adapter == null) {
                throw new IllegalArgumentException("No adapter found for board: " + boardName +
                    ". Available: " + adapters.keySet());
            }

            proxyManager.initialize();
            var proxy = proxyManager.getNextProxy();

            multiTabManager.initialize();
            framework.initialize("chromium", headless, proxy);

            var sessionId = UUID.randomUUID().toString();
            currentSessionId = sessionId;

            if (cookiePersistenceManager.hasCookies(boardName)) {
                cookiePersistenceManager.loadCookies(browserManager, boardName);
                log.info("Loaded saved cookies for {}", boardName);
            }

            stealthManager.applyStealthSettings(browserManager);

            var session = framework.startSession(boardName, credentials);

            progressTracker.trackStart(sessionId, adapter.loginFlow().loginUrl(), "Login");

            try {
                smartRetryManager.executeWithStrategy(
                    "LOGIN",
                    () -> framework.executeApplication(session, adapter, credentials, adapter.loginFlow().loginUrl()),
                    "Login to " + boardName
                );
                proxyManager.reportSuccess(proxy);
            } catch (Exception e) {
                proxyManager.reportFailure(proxy);
                throw e;
            }

            cookiePersistenceManager.saveCookies(browserManager, boardName);

            progressTracker.trackStep(sessionId, "logged_in", "Successfully logged into " + boardName);

            int processed = 0;
            while (running && !applicationQueue.isEmpty()) {
                var requestOpt = applicationQueue.dequeue();
                if (requestOpt.isEmpty()) break;

                processed++;
                processApplication(session, adapter, requestOpt.get(), userId, missionId, sessionId, processed);
            }

            if (running) {
                progressTracker.trackComplete(sessionId, "completed");
                log.info("Automation completed. Processed {} applications", processed);
            }

        } catch (Exception e) {
            log.error("Automation failed: {}", e.getMessage(), e);
            if (currentSessionId != null) {
                progressTracker.trackError(currentSessionId, e.getMessage());
                progressTracker.trackComplete(currentSessionId, "failed");
            }
        } finally {
            running = false;
            currentSessionId = null;
            multiTabManager.closeAllTabs();
            framework.cleanup();
            log.info("Automation session ended");
        }
    }

    private void processApplication(BrowserSession session, JobBoardAdapter adapter,
                                     JobApplicationRequest request, UUID userId,
                                     UUID missionId, String sessionId, int index) {
        var jobUrl = request.jobUrl();
        var jobTitle = request.jobTitle();
        var companyName = request.companyName();

        log.info("[{}/{}] Processing: {} at {}", index, applicationQueue.size() + index, jobTitle, companyName);
        progressTracker.trackStart(sessionId, jobUrl, jobTitle);

        var result = ApplicationResultJpaEntity.createPending(userId, missionId,
            UUID.fromString(sessionId));
        result.setJobUrl(jobUrl);
        result.setJobTitle(jobTitle);
        result.setCompanyName(companyName);
        resultRepository.save(result);

        try {
            progressTracker.trackStep(sessionId, "navigating", "Opening: " + jobUrl);

            var applicationResult = smartRetryManager.executeWithStrategy(
                "APPLICATION",
                () -> framework.executeApplication(session, adapter, request.userProfile(), jobUrl),
                "Apply to " + jobTitle
            );

            var outcome = applicationResult.outcome();
            if (outcome == ApplicationOutcome.SUBMITTED || outcome == ApplicationOutcome.SUCCESS) {
                result.markSubmitted();
                progressTracker.trackComplete(sessionId, "submitted");
                log.info("Successfully applied to {}", jobTitle);
            } else if (outcome == ApplicationOutcome.REQUIRES_CAPTCHA) {
                result.markRequiresCaptcha();
                progressTracker.trackCaptchaWaiting(sessionId);
                log.warn("CAPTCHA required for {}", jobTitle);
            } else {
                result.markFailed(applicationResult.errorMessage());
                progressTracker.trackError(sessionId, applicationResult.errorMessage());
                log.warn("Failed to apply to {}: {}", jobTitle, applicationResult.errorMessage());
            }

            resultRepository.save(result);

        } catch (Exception e) {
            var errorType = smartRetryManager.classifyError(e);
            log.error("Application failed for {} ({}): {}", jobTitle, errorType, e.getMessage());

            result.markFailed(e.getMessage());
            resultRepository.save(result);
            progressTracker.trackError(sessionId, e.getMessage());
        }
    }

    public void stop() {
        running = false;
        log.info("Stop requested for automation");
    }

    public boolean isRunning() {
        return running;
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }

    public int getQueueSize() {
        return applicationQueue.size();
    }

    public List<ApplicationResultJpaEntity> getResultsByMission(UUID missionId) {
        return resultRepository.findByMissionId(missionId.toString());
    }

    public List<ApplicationResultJpaEntity> getResultsByOutcome(String outcome) {
        return resultRepository.findByOutcome(outcome);
    }

    public List<ApplicationResultJpaEntity> getRecentResults() {
        return resultRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<String> getAvailableBoards() {
        return List.copyOf(adapters.keySet());
    }

    public boolean isHeadlessMode() {
        return defaultHeadless;
    }

    public Map<String, Object> getProxyStats() {
        var stats = new HashMap<String, Object>();
        stats.put("enabled", proxyManager.isEnabled());
        stats.put("poolSize", proxyManager.getPoolSize());
        stats.put("activeProxies", proxyManager.getActiveProxies().size());
        stats.put("stats", proxyManager.getStats());
        return stats;
    }

    private final Map<String, String> pendingCaptchaSolutions = new ConcurrentHashMap<>();

    public boolean resolveCaptcha(String sessionId, String solution) {
        if (sessionId == null || solution == null) return false;
        pendingCaptchaSolutions.put(sessionId, solution);
        progressTracker.trackCaptchaResolved(sessionId);
        log.info("CAPTCHA solution received for session: {}", sessionId);
        return true;
    }

    public String getCaptchaSolution(String sessionId) {
        return pendingCaptchaSolutions.remove(sessionId);
    }

    public boolean hasCaptchaSolution(String sessionId) {
        return pendingCaptchaSolutions.containsKey(sessionId);
    }
}
