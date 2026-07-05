package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {

    @Mock
    private com.jobpilot.application.automation.ports.BrowserPort browserPort;

    @InjectMocks
    private SessionManager sessionManager;

    @Test
    void shouldCreateSession() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());

        assertNotNull(session);
        assertEquals("LinkedIn", session.adapterName());
        assertEquals(SessionStatus.CREATED, session.status());
    }

    @Test
    void shouldGetSession() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
    }

    @Test
    void shouldActivateSession() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.activateSession(session.sessionId().value().toString());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertTrue(found.get().isActive());
    }

    @Test
    void shouldPauseSession() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.activateSession(session.sessionId().value().toString());
        sessionManager.pauseSession(session.sessionId().value().toString());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertEquals(SessionStatus.PAUSED, found.get().status());
    }

    @Test
    void shouldCompleteSession() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.activateSession(session.sessionId().value().toString());
        sessionManager.completeSession(session.sessionId().value().toString());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertEquals(SessionStatus.COMPLETED, found.get().status());
    }

    @Test
    void shouldFailSession() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.failSession(session.sessionId().value().toString(), "Test error");

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertEquals(SessionStatus.FAILED, found.get().status());
        assertEquals("Test error", found.get().errorMessage());
    }

    @Test
    void shouldPauseForCaptcha() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.activateSession(session.sessionId().value().toString());
        sessionManager.pauseForCaptcha(session.sessionId().value().toString());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertEquals(SessionStatus.WAITING_FOR_CAPTCHA, found.get().status());
    }

    @Test
    void shouldResumeFromCaptcha() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.activateSession(session.sessionId().value().toString());
        sessionManager.pauseForCaptcha(session.sessionId().value().toString());
        sessionManager.resumeFromCaptcha(session.sessionId().value().toString());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertEquals(SessionStatus.ACTIVE, found.get().status());
    }

    @Test
    void shouldCloseAndRemoveSession() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.closeSession(session.sessionId().value().toString());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertFalse(found.isPresent());
    }

    @Test
    void shouldTrackActiveSessions() {
        var s1 = sessionManager.createSession("LinkedIn", java.util.Map.of());
        var s2 = sessionManager.createSession("Indeed", java.util.Map.of());
        sessionManager.activateSession(s1.sessionId().value().toString());
        sessionManager.activateSession(s2.sessionId().value().toString());

        assertEquals(2, sessionManager.getActiveSessionCount());
    }

    @Test
    void shouldIncrementRetries() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.incrementRetries(session.sessionId().value().toString());

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().errorsEncountered());
    }

    @Test
    void shouldSetLastError() {
        var session = sessionManager.createSession("LinkedIn", java.util.Map.of());
        sessionManager.setLastError(session.sessionId().value().toString(), "Test error");

        var found = sessionManager.getSession(session.sessionId().value().toString());
        assertTrue(found.isPresent());
        assertEquals("Test error", found.get().errorMessage());
    }
}
