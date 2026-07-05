package com.jobpilot.infrastructure.automation.lever;

import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeverAdapterTest {

    private LeverAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LeverAdapter(null, null);
    }

    @Test
    @DisplayName("Should have correct name and base URL")
    void shouldHaveCorrectNameAndBaseUrl() {
        assertEquals("Lever", adapter.name());
        assertEquals("https://jobs.lever.co", adapter.baseUrl());
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
        assertEquals(25, caps.maxResultsPerPage());
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
        assertEquals(".posting", search.jobCardSelector());
        assertEquals(".posting a.posting-name", search.jobTitleSelector());
        assertEquals(".posting .posting-company", search.companyNameSelector());
        assertEquals(".posting .posting-category-primary", search.locationSelector());
    }

    @Test
    @DisplayName("Should have correct application flow selectors")
    void shouldHaveCorrectApplicationFlow() {
        var app = adapter.applicationFlow();
        assertEquals(".posting-btn-submit", app.easyApplyButtonSelector());
        assertEquals(".posting-btn-submit", app.applyButtonSelector());
        assertEquals(".posting-btn-submit", app.submitButtonSelector());
        assertEquals(".application-form", app.formContainerSelector());
        assertEquals("input[type='file']", app.fileUploadSelector());
    }

    @Test
    @DisplayName("Should have correct field selectors")
    void shouldHaveCorrectFieldSelectors() {
        var fields = adapter.applicationFlow().fieldSelectors();
        assertEquals(".application-name input", fields.get("name"));
        assertEquals(".application-email input", fields.get("email"));
        assertEquals(".application-phone input", fields.get("phone"));
        assertEquals("input[type='file'][name='resume']", fields.get("resume"));
        assertEquals("input[type='file'][name='comments']", fields.get("cover_letter"));
        assertEquals(".application-linkedin input", fields.get("linkedin"));
        assertEquals(".application-urls input", fields.get("website"));
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
