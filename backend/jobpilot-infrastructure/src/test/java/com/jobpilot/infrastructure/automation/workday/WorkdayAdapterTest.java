package com.jobpilot.infrastructure.automation.workday;

import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkdayAdapterTest {

    @Mock
    private PlaywrightBrowserManager browserManager;

    @Mock
    private SessionManager sessionManager;

    private WorkdayAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkdayAdapter(browserManager, sessionManager);
    }

    @Test
    @DisplayName("Should have correct board name")
    void shouldHaveCorrectName() {
        assertEquals("Workday", adapter.name());
    }

    @Test
    @DisplayName("Should have correct base URL")
    void shouldHaveCorrectBaseUrl() {
        assertEquals("https://www.myworkdayjobs.com", adapter.baseUrl());
    }

    @Test
    @DisplayName("Should support direct apply")
    void shouldSupportDirectApply() {
        assertTrue(adapter.capabilities().supportsDirectApply());
    }

    @Test
    @DisplayName("Should support login")
    void shouldSupportLogin() {
        assertTrue(adapter.capabilities().supportsLogin());
    }

    @Test
    @DisplayName("Should require authentication")
    void shouldRequireAuthentication() {
        assertTrue(adapter.requiresAuthentication());
    }

    @Test
    @DisplayName("Should have search flow with input selector")
    void shouldHaveSearchFlow() {
        assertNotNull(adapter.searchFlow());
        assertNotNull(adapter.searchFlow().searchInputSelector());
    }

    @Test
    @DisplayName("Should have login flow with email selector")
    void shouldHaveLoginFlow() {
        assertNotNull(adapter.loginFlow());
        assertNotNull(adapter.loginFlow().usernameSelector());
    }

    @Test
    @DisplayName("Should have application flow with apply button")
    void shouldHaveApplicationFlow() {
        assertNotNull(adapter.applicationFlow());
        assertNotNull(adapter.applicationFlow().applyButtonSelector());
    }

    @Test
    @DisplayName("Should have reasonable request delay")
    void shouldHaveReasonableDelay() {
        assertTrue(adapter.requestDelayMs() >= 1000);
        assertTrue(adapter.requestDelayMs() <= 5000);
    }

    @Test
    @DisplayName("Should have field selectors for common fields")
    void shouldHaveFieldSelectors() {
        var fields = adapter.applicationFlow().fieldSelectors();
        assertNotNull(fields);
        assertTrue(fields.containsKey("email"));
        assertTrue(fields.containsKey("firstName"));
        assertTrue(fields.containsKey("lastName"));
    }
}
