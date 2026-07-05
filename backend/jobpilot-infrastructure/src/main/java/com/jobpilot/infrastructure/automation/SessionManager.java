package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.SessionStatus;
import com.jobpilot.application.automation.ports.BrowserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final BrowserPort browserPort;
    private final Map<String, BrowserSession> sessions = new ConcurrentHashMap<>();

    public SessionManager(BrowserPort browserPort) {
        this.browserPort = browserPort;
    }

    public BrowserSession createSession(String boardName, Map<String, String> credentials) {
        var session = BrowserSession.create(null, null, boardName);
        sessions.put(session.getId().value().toString(), session);

        log.info("Created session {} for board {}", session.getId().value(), boardName);
        return session;
    }

    public Optional<BrowserSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void activateSession(String sessionId) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.activate();
            log.info("Activated session {}", sessionId);
        }
    }

    public void pauseSession(String sessionId) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.pause();
            log.info("Paused session {}", sessionId);
        }
    }

    public void completeSession(String sessionId) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.complete();
            log.info("Completed session {}", sessionId);
        }
    }

    public void failSession(String sessionId, String reason) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.fail(reason);
            log.info("Failed session {}: {}", sessionId, reason);
        }
    }

    public void pauseForCaptcha(String sessionId) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.pauseForCaptcha();
            log.info("Session {} paused for CAPTCHA", sessionId);
        }
    }

    public void resumeFromCaptcha(String sessionId) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.resumeFromCaptcha();
            log.info("Session {} resumed from CAPTCHA", sessionId);
        }
    }

    public void closeSession(String sessionId) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.close();
            sessions.remove(sessionId);
            log.info("Closed and removed session {}", sessionId);
        }
    }

    public boolean isSessionActive(String sessionId) {
        var session = sessions.get(sessionId);
        return session != null && session.isActive();
    }

    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
            .filter(BrowserSession::isActive)
            .count();
    }

    public void incrementRetries(String sessionId) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.incrementRetries();
        }
    }

    public boolean hasExceededMaxRetries(String sessionId, int maxRetries) {
        var session = sessions.get(sessionId);
        return session != null && session.hasExceededMaxRetries(maxRetries);
    }

    public void setLastError(String sessionId, String error) {
        var session = sessions.get(sessionId);
        if (session != null) {
            session.setLastError(error);
        }
    }

    public Optional<String> getLastError(String sessionId) {
        var session = sessions.get(sessionId);
        return session != null ? Optional.ofNullable(session.getLastError()) : Optional.empty();
    }
}
