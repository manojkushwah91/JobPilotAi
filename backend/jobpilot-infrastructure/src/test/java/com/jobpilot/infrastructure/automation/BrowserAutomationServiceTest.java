package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.JobBoardAdapter;
import com.jobpilot.infrastructure.automation.multitab.MultiTabManager;
import com.jobpilot.infrastructure.automation.persistence.CookiePersistenceManager;
import com.jobpilot.infrastructure.automation.progress.AutomationProgressTracker;
import com.jobpilot.infrastructure.automation.proxy.ProxyManager;
import com.jobpilot.infrastructure.automation.queue.ApplicationQueue;
import com.jobpilot.infrastructure.automation.retry.SmartRetryManager;
import com.jobpilot.infrastructure.automation.stealth.StealthManager;
import com.jobpilot.infrastructure.persistence.automation.ApplicationResultJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrowserAutomationServiceTest {

    @Mock private BrowserAutomationFramework framework;
    @Mock private ApplicationQueue applicationQueue;
    @Mock private AutomationProgressTracker progressTracker;
    @Mock private StealthManager stealthManager;
    @Mock private MultiTabManager multiTabManager;
    @Mock private SmartRetryManager smartRetryManager;
    @Mock private CookiePersistenceManager cookiePersistenceManager;
    @Mock private ApplicationResultJpaRepository resultRepository;
    @Mock private ProxyManager proxyManager;

    private BrowserAutomationService service;

    @BeforeEach
    void setUp() {
        service = new BrowserAutomationService(
            framework,
            applicationQueue,
            progressTracker,
            stealthManager,
            multiTabManager,
            smartRetryManager,
            cookiePersistenceManager,
            resultRepository,
            proxyManager,
            List.of()
        );
    }

    @Test
    @DisplayName("Should report not running initially")
    void shouldNotBeRunningInitially() {
        assertFalse(service.isRunning());
        assertNull(service.getCurrentSessionId());
    }

    @Test
    @DisplayName("Should report correct available boards (empty)")
    void shouldReportAvailableBoards() {
        var boards = service.getAvailableBoards();
        assertNotNull(boards);
        assertTrue(boards.isEmpty());
    }

    @Test
    @DisplayName("Should report queue size from application queue")
    void shouldReportQueueSize() {
        when(applicationQueue.size()).thenReturn(5);
        assertEquals(5, service.getQueueSize());
    }

    @Test
    @DisplayName("Should stop automation")
    void shouldStopAutomation() {
        service.stop();
        assertFalse(service.isRunning());
    }

    @Test
    @DisplayName("Should report running state correctly")
    void shouldReportRunningState() {
        assertFalse(service.isRunning());
        service.stop();
        assertFalse(service.isRunning());
    }

    @Test
    @DisplayName("Should get results by mission ID")
    void shouldGetResultsByMission() {
        var missionId = UUID.randomUUID();
        when(resultRepository.findByMissionId(missionId.toString())).thenReturn(List.of());

        var results = service.getResultsByMission(missionId);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(resultRepository).findByMissionId(missionId.toString());
    }

    @Test
    @DisplayName("Should get results by outcome")
    void shouldGetResultsByOutcome() {
        when(resultRepository.findByOutcome("SUBMITTED")).thenReturn(List.of());

        var results = service.getResultsByOutcome("SUBMITTED");
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(resultRepository).findByOutcome("SUBMITTED");
    }

    @Test
    @DisplayName("Should report boards with adapter")
    void shouldReportBoardsWithAdapter() {
        var adapter = mock(JobBoardAdapter.class);
        when(adapter.name()).thenReturn("LinkedIn");

        var serviceWithAdapter = new BrowserAutomationService(
            framework, applicationQueue, progressTracker,
            stealthManager, multiTabManager, smartRetryManager,
            cookiePersistenceManager, resultRepository, proxyManager,
            List.of(adapter)
        );

        var boards = serviceWithAdapter.getAvailableBoards();
        assertEquals(1, boards.size());
        assertTrue(boards.contains("linkedin"));
    }

    @Test
    @DisplayName("Should catch exception when running automation for unknown board")
    void shouldCatchExceptionForUnknownBoard() {
        when(framework.getBrowserManager()).thenReturn(mock(PlaywrightBrowserManager.class));

        service.runAutomation("Unknown", Map.of(), UUID.randomUUID(), UUID.randomUUID());

        assertFalse(service.isRunning());
        assertNull(service.getCurrentSessionId());
    }

    @Test
    @DisplayName("Should get null current session before starting")
    void shouldGetNullCurrentSessionBeforeStarting() {
        assertNull(service.getCurrentSessionId());
    }

    @Test
    @DisplayName("Should report proxy stats")
    void shouldReportProxyStats() {
        when(proxyManager.isEnabled()).thenReturn(true);
        when(proxyManager.getPoolSize()).thenReturn(5);
        when(proxyManager.getActiveProxies()).thenReturn(List.of("p1", "p2"));

        var stats = service.getProxyStats();
        assertEquals(true, stats.get("enabled"));
        assertEquals(5, stats.get("poolSize"));
        assertEquals(2, stats.get("activeProxies"));
    }

    @Test
    @DisplayName("Should report headless mode from default value")
    void shouldReportHeadlessMode() {
        var serviceWithHeadless = new BrowserAutomationService(
            framework, applicationQueue, progressTracker,
            stealthManager, multiTabManager, smartRetryManager,
            cookiePersistenceManager, resultRepository, proxyManager,
            List.of()
        );
        assertNotNull(serviceWithHeadless);
    }
}
