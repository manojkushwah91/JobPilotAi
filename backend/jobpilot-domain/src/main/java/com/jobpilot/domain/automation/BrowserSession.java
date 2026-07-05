package com.jobpilot.domain.automation;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BrowserSession extends BaseAggregateRoot {

    private SessionId sessionId;
    private UUID userId;
    private UUID missionId;
    private String adapterName;
    private SessionStatus status;
    private String currentPageUrl;
    private String currentPageTitle;
    private int pagesVisited;
    private int applicationsSubmitted;
    private int errorsEncountered;
    private List<PageAction> actionHistory;
    private Map<String, String> cookies;
    private Map<String, Object> sessionData;
    private String errorMessage;
    private Instant lastActivityAt;
    private Instant createdAt;
    private Instant updatedAt;

    private BrowserSession(SessionId sessionId, UUID userId, UUID missionId, String adapterName) {
        super(sessionId.value());
        this.sessionId = sessionId;
        this.userId = userId;
        this.missionId = missionId;
        this.adapterName = adapterName;
        this.status = SessionStatus.CREATED;
        this.pagesVisited = 0;
        this.applicationsSubmitted = 0;
        this.errorsEncountered = 0;
        this.actionHistory = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static BrowserSession create(UUID userId, UUID missionId, String adapterName) {
        return new BrowserSession(SessionId.generate(), userId, missionId, adapterName);
    }

    public static BrowserSession reconstitute(SessionId sessionId, UUID userId, UUID missionId,
                                                String adapterName, SessionStatus status,
                                                String currentPageUrl, String currentPageTitle,
                                                int pagesVisited, int applicationsSubmitted,
                                                int errorsEncountered, List<PageAction> actionHistory,
                                                Map<String, String> cookies, Map<String, Object> sessionData,
                                                String errorMessage, Instant lastActivityAt,
                                                Instant createdAt, Instant updatedAt) {
        var s = new BrowserSession(sessionId, userId, missionId, adapterName);
        s.status = status;
        s.currentPageUrl = currentPageUrl;
        s.currentPageTitle = currentPageTitle;
        s.pagesVisited = pagesVisited;
        s.applicationsSubmitted = applicationsSubmitted;
        s.errorsEncountered = errorsEncountered;
        s.actionHistory = actionHistory != null ? new ArrayList<>(actionHistory) : new ArrayList<>();
        s.cookies = cookies;
        s.sessionData = sessionData;
        s.errorMessage = errorMessage;
        s.lastActivityAt = lastActivityAt;
        s.createdAt = createdAt;
        s.updatedAt = updatedAt;
        return s;
    }

    public void start() {
        this.status = SessionStatus.ACTIVE;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = SessionStatus.ACTIVE;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void pause() {
        this.status = SessionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public void resume() {
        this.status = SessionStatus.ACTIVE;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = SessionStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void fail(String error) {
        this.status = SessionStatus.FAILED;
        this.errorMessage = error;
        this.updatedAt = Instant.now();
    }

    public void close() {
        this.status = SessionStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    public void pauseForCaptcha() {
        this.status = SessionStatus.WAITING_FOR_CAPTCHA;
        this.updatedAt = Instant.now();
    }

    public void resumeFromCaptcha() {
        this.status = SessionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void incrementRetries() {
        this.errorsEncountered++;
        this.updatedAt = Instant.now();
    }

    public boolean hasExceededMaxRetries(int maxRetries) {
        return this.errorsEncountered >= maxRetries;
    }

    public void setLastError(String error) {
        this.errorMessage = error;
        this.updatedAt = Instant.now();
    }

    public void recordScreenshot(byte[] screenshot) {
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }

    public String getBoardName() {
        return this.adapterName;
    }

    public SessionId getId() {
        return this.sessionId;
    }

    public String getCurrentUrl() {
        return this.currentPageUrl;
    }

    public int getRetryCount() {
        return this.errorsEncountered;
    }

    public String getLastError() {
        return this.errorMessage;
    }

    public java.time.LocalDateTime getClosedAt() {
        return null;
    }

    public void recordAction(PageAction action) {
        actionHistory.add(action);
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void incrementPagesVisited() {
        this.pagesVisited++;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void incrementApplicationsSubmitted() {
        this.applicationsSubmitted++;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void incrementErrors() {
        this.errorsEncountered++;
        this.updatedAt = Instant.now();
    }

    public void updateCurrentPage(String url, String title) {
        this.currentPageUrl = url;
        this.currentPageTitle = title;
        this.lastActivityAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public SessionId sessionId() { return sessionId; }
    public UUID userId() { return userId; }
    public UUID missionId() { return missionId; }
    public String adapterName() { return adapterName; }
    public SessionStatus status() { return status; }
    public String currentPageUrl() { return currentPageUrl; }
    public String currentPageTitle() { return currentPageTitle; }
    public int pagesVisited() { return pagesVisited; }
    public int applicationsSubmitted() { return applicationsSubmitted; }
    public int errorsEncountered() { return errorsEncountered; }
    public List<PageAction> actionHistory() { return List.copyOf(actionHistory); }
    public Map<String, String> cookies() { return cookies; }
    public Map<String, Object> sessionData() { return sessionData; }
    public String errorMessage() { return errorMessage; }
    public Instant lastActivityAt() { return lastActivityAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
