package com.jobpilot.infrastructure.automation.progress;

import com.jobpilot.infrastructure.websocket.AutomationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AutomationProgressTracker {

    private static final Logger log = LoggerFactory.getLogger(AutomationProgressTracker.class);

    private final AutomationWebSocketHandler webSocketHandler;
    private final Map<String, AutomationProgress> progressMap = new ConcurrentHashMap<>();

    public AutomationProgressTracker(AutomationWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public void trackStart(String sessionId, String jobUrl, String jobTitle) {
        var progress = new AutomationProgress(sessionId, jobUrl, jobTitle);
        progressMap.put(sessionId, progress);
        broadcastUpdate(sessionId, "started", progress);
    }

    public void trackStep(String sessionId, String step, String detail) {
        var progress = progressMap.get(sessionId);
        if (progress != null) {
            progress.currentStep = step;
            progress.detail = detail;
            progress.stepHistory.add(new StepRecord(step, detail, Instant.now()));
            broadcastUpdate(sessionId, "step", progress);
        }
    }

    public void trackScreenshot(String sessionId, String screenshotPath) {
        var progress = progressMap.get(sessionId);
        if (progress != null) {
            progress.lastScreenshot = screenshotPath;
            broadcastUpdate(sessionId, "screenshot", progress);
        }
    }

    public void trackError(String sessionId, String error) {
        var progress = progressMap.get(sessionId);
        if (progress != null) {
            progress.lastError = error;
            progress.errorCount++;
            broadcastUpdate(sessionId, "error", progress);
        }
    }

    public void trackComplete(String sessionId, String outcome) {
        var progress = progressMap.get(sessionId);
        if (progress != null) {
            progress.outcome = outcome;
            progress.completedAt = Instant.now();
            broadcastUpdate(sessionId, "completed", progress);
        }
    }

    public void trackCaptchaWaiting(String sessionId) {
        var progress = progressMap.get(sessionId);
        if (progress != null) {
            progress.waitingForCaptcha = true;
            broadcastUpdate(sessionId, "captcha_waiting", progress);
        }
    }

    public void trackCaptchaResolved(String sessionId) {
        var progress = progressMap.get(sessionId);
        if (progress != null) {
            progress.waitingForCaptcha = false;
            broadcastUpdate(sessionId, "captcha_resolved", progress);
        }
    }

    public Optional<AutomationProgress> getProgress(String sessionId) {
        return Optional.ofNullable(progressMap.get(sessionId));
    }

    public List<AutomationProgress> getAllProgress() {
        return new ArrayList<>(progressMap.values());
    }

    public void removeProgress(String sessionId) {
        progressMap.remove(sessionId);
    }

    private void broadcastUpdate(String sessionId, String type, AutomationProgress progress) {
        var data = new HashMap<String, Object>();
        data.put("sessionId", sessionId);
        data.put("type", type);
        data.put("jobUrl", progress.jobUrl);
        data.put("jobTitle", progress.jobTitle);
        data.put("currentStep", progress.currentStep);
        data.put("detail", progress.detail);
        data.put("outcome", progress.outcome);
        data.put("errorCount", progress.errorCount);
        data.put("waitingForCaptcha", progress.waitingForCaptcha);
        data.put("timestamp", Instant.now().toString());

        webSocketHandler.broadcastToTopic("automation_progress", data);
    }

    public static class AutomationProgress {
        public final String sessionId;
        public final String jobUrl;
        public final String jobTitle;
        public String currentStep;
        public String detail;
        public String outcome;
        public String lastScreenshot;
        public String lastError;
        public int errorCount;
        public boolean waitingForCaptcha;
        public final Instant startedAt;
        public Instant completedAt;
        public final List<StepRecord> stepHistory = new ArrayList<>();

        public AutomationProgress(String sessionId, String jobUrl, String jobTitle) {
            this.sessionId = sessionId;
            this.jobUrl = jobUrl;
            this.jobTitle = jobTitle;
            this.startedAt = Instant.now();
        }
    }

    public record StepRecord(String step, String detail, Instant timestamp) {}
}
