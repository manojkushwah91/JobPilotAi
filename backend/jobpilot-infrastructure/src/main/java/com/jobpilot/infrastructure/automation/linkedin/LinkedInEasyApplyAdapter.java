package com.jobpilot.infrastructure.automation.linkedin;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.JobBoardAdapter;
import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LinkedInEasyApplyAdapter implements JobBoardAdapter {

    private static final Logger log = LoggerFactory.getLogger(LinkedInEasyApplyAdapter.class);

    private final PlaywrightBrowserManager browserManager;
    private final SessionManager sessionManager;

    public LinkedInEasyApplyAdapter(PlaywrightBrowserManager browserManager, SessionManager sessionManager) {
        this.browserManager = browserManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public String name() {
        return "LinkedIn";
    }

    @Override
    public String baseUrl() {
        return "https://www.linkedin.com";
    }

    @Override
    public AdapterCapabilities capabilities() {
        return new AdapterCapabilities(
            true,   // supportsEasyApply
            true,   // supportsDirectApply
            true,   // supportsLogin
            true,   // supportsSearch
            true,   // supportsPagination
            true,   // supportsFilters
            25      // maxResultsPerPage
        );
    }

    @Override
    public LoginFlow loginFlow() {
        return new LoginFlow(
            "https://www.linkedin.com/login",
            "#username",
            "#password",
            "button[type='submit']",
            List.of("/feed", "/jobs"),
            List.of("iframe[src*='recaptcha']", ".g-recaptcha")
        );
    }

    @Override
    public SearchFlow searchFlow() {
        return new SearchFlow(
            "https://www.linkedin.com/jobs/search/",
            "input[aria-label='Search by title, skill, or company']",
            "button[aria-label='Search']",
            ".job-card-container",
            ".job-card-list__title",
            ".job-card-container__primary-description",
            ".job-card-container__metadata-item",
            ".jobs-apply-badge",
            ".jobs-search-results__pagination",
            "button[aria-label='View next page']"
        );
    }

    @Override
    public ApplicationFlow applicationFlow() {
        return new ApplicationFlow(
            ".jobs-apply-button",
            ".jobs-apply-button",
            "button[aria-label='Continue to next step']",
            "button[aria-label='Submit application']",
            "button[aria-label='Review your application']",
            ".jobs-easy-apply-modal",
            "input[type='file']",
            ".jobs-easy-apply-modal__question",
            ".artdeco-toast-item",
            ".jobs-easy-apply-modal__error",
            Map.of(
                "email", "#email-address-input",
                "phone", "#phone-number-input",
                "name", "#name-input"
            )
        );
    }
}
