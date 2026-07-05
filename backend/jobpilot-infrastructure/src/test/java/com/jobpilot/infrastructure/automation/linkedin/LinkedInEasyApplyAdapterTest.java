package com.jobpilot.infrastructure.automation.linkedin;

import com.jobpilot.domain.automation.BrowserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkedInEasyApplyAdapterTest {

    @Mock
    private com.jobpilot.infrastructure.automation.PlaywrightBrowserManager browserManager;

    @Mock
    private com.jobpilot.infrastructure.automation.SessionManager sessionManager;

    @InjectMocks
    private LinkedInEasyApplyAdapter adapter;

    private BrowserSession testSession;

    @BeforeEach
    void setUp() {
        testSession = BrowserSession.create(null, null, "LinkedIn");
    }

    @Test
    void shouldReturnLinkedInBoardName() {
        assertEquals("LinkedIn", adapter.name());
    }

    @Test
    void shouldReturnLinkedInBaseUrl() {
        assertEquals("https://www.linkedin.com", adapter.baseUrl());
    }

    @Test
    void shouldHaveCapabilities() {
        assertNotNull(adapter.capabilities());
        assertTrue(adapter.capabilities().supportsEasyApply());
        assertTrue(adapter.capabilities().supportsLogin());
        assertTrue(adapter.capabilities().supportsSearch());
    }

    @Test
    void shouldCreateLoginFlow() {
        assertNotNull(adapter.loginFlow());
    }

    @Test
    void shouldCreateSearchFlow() {
        assertNotNull(adapter.searchFlow());
    }

    @Test
    void shouldCreateApplicationFlow() {
        assertNotNull(adapter.applicationFlow());
    }

    @Test
    void shouldHaveLoginUrl() {
        assertEquals("https://www.linkedin.com/login", adapter.loginFlow().loginUrl());
    }

    @Test
    void shouldHaveSearchUrl() {
        assertEquals("https://www.linkedin.com/jobs/search/", adapter.searchFlow().searchUrl());
    }
}
