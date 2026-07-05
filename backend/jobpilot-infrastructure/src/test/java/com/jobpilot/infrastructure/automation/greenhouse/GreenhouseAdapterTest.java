package com.jobpilot.infrastructure.automation.greenhouse;

import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreenhouseAdapterTest {

    private GreenhouseAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GreenhouseAdapter(null, null);
    }

    @Test
    @DisplayName("Should have correct name and base URL")
    void shouldHaveCorrectNameAndBaseUrl() {
        assertEquals("Greenhouse", adapter.name());
        assertEquals("https://boards.greenhouse.io", adapter.baseUrl());
    }

    @Test
    @DisplayName("Should not require authentication")
    void shouldNotRequireAuthentication() {
        assertFalse(adapter.requiresAuthentication());
    }

    @Test
    @DisplayName("Should have correct capabilities")
    void shouldHaveCorrectCapabilities() {
        var caps = adapter.capabilities();
        assertFalse(caps.supportsEasyApply());
        assertTrue(caps.supportsDirectApply());
        assertFalse(caps.supportsLogin());
        assertTrue(caps.supportsSearch());
        assertFalse(caps.supportsPagination());
        assertTrue(caps.supportsFilters());
        assertEquals(20, caps.maxResultsPerPage());
    }

    @Test
    @DisplayName("Should have empty login flow for non-auth board")
    void shouldHaveEmptyLoginFlow() {
        var login = adapter.loginFlow();
        assertEquals("", login.loginUrl());
        assertEquals("", login.usernameSelector());
        assertEquals("", login.passwordSelector());
        assertEquals("", login.submitSelector());
        assertTrue(login.postLoginIndicators().isEmpty());
        assertTrue(login.captchaIndicators().isEmpty());
    }

    @Test
    @DisplayName("Should have correct search selectors")
    void shouldHaveCorrectSearchSelectors() {
        var search = adapter.searchFlow();
        assertEquals(".opening", search.jobCardSelector());
        assertEquals(".opening h4", search.jobTitleSelector());
        assertEquals(".opening .company", search.companyNameSelector());
        assertEquals(".location", search.locationSelector());
    }

    @Test
    @DisplayName("Should have correct application flow selectors")
    void shouldHaveCorrectApplicationFlow() {
        var app = adapter.applicationFlow();
        assertEquals("a[href*='apply']", app.easyApplyButtonSelector());
        assertEquals("#submit_app", app.applyButtonSelector());
        assertEquals("#submit_app", app.submitButtonSelector());
        assertEquals("#application_form", app.formContainerSelector());
        assertEquals("input[type='file']", app.fileUploadSelector());
    }

    @Test
    @DisplayName("Should have correct field selectors")
    void shouldHaveCorrectFieldSelectors() {
        var fields = adapter.applicationFlow().fieldSelectors();
        assertEquals("#first_name", fields.get("first_name"));
        assertEquals("#last_name", fields.get("last_name"));
        assertEquals("#email", fields.get("email"));
        assertEquals("#phone", fields.get("phone"));
        assertEquals("input[type='file'][name='resume']", fields.get("resume"));
        assertEquals("input[type='file'][name='cover_letter']", fields.get("cover_letter"));
        assertEquals("#url", fields.get("linkedin"));
        assertEquals("#website", fields.get("website"));
    }

    @Test
    @DisplayName("Should have standard job types")
    void shouldHaveStandardJobTypes() {
        var types = adapter.supportedJobTypes();
        assertEquals(3, types.size());
        assertTrue(types.contains("FULL_TIME"));
        assertTrue(types.contains("PART_TIME"));
        assertTrue(types.contains("CONTRACT"));
    }

    @Test
    @DisplayName("Should have correct max concurrent sessions")
    void shouldHaveCorrectMaxConcurrentSessions() {
        assertEquals(3, adapter.maxConcurrentSessions());
    }

    @Test
    @DisplayName("Should have correct request delay")
    void shouldHaveCorrectRequestDelay() {
        assertEquals(1500, adapter.requestDelayMs());
    }
}
