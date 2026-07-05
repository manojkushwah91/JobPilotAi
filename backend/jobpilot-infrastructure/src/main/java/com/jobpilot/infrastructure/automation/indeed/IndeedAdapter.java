package com.jobpilot.infrastructure.automation.indeed;

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
public class IndeedAdapter implements JobBoardAdapter {

    private static final Logger log = LoggerFactory.getLogger(IndeedAdapter.class);

    private final PlaywrightBrowserManager browserManager;
    private final SessionManager sessionManager;

    public IndeedAdapter(PlaywrightBrowserManager browserManager, SessionManager sessionManager) {
        this.browserManager = browserManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public String name() {
        return "Indeed";
    }

    @Override
    public String baseUrl() {
        return "https://www.indeed.com";
    }

    @Override
    public AdapterCapabilities capabilities() {
        return new AdapterCapabilities(
            false,  // supportsEasyApply (Indeed doesn't have Easy Apply)
            true,   // supportsDirectApply
            true,   // supportsLogin
            true,   // supportsSearch
            true,   // supportsPagination
            true,   // supportsFilters
            15      // maxResultsPerPage
        );
    }

    @Override
    public LoginFlow loginFlow() {
        return new LoginFlow(
            "https://secure.indeed.com/auth",
            "#iuserloginid",
            "#ipassword",
            "#loginform input[type='submit']",
            List.of("/jobs", "/viewjob"),
            List.of("#captcha-challenge", ".g-recaptcha")
        );
    }

    @Override
    public SearchFlow searchFlow() {
        return new SearchFlow(
            "https://www.indeed.com/jobs",
            "#text-input-what",
            ".jobsearch-SearchBar-button",
            ".jobsearch-ResultsList .result",
            ".jobTitle a",
            ".companyName",
            ".companyLocation",
            null,
            ".pagination",
            ".pagination a[aria-label='Next Page']"
        );
    }

    @Override
    public ApplicationFlow applicationFlow() {
        return new ApplicationFlow(
            "#indeed-apply-button",
            "#indeed-apply-button",
            "#form-step-continue-button",
            "#form-submit-button",
            "#review-step-submit-button",
            ".ia-modal",
            "input[type='file']",
            ".ia-questions",
            ".ia-success",
            ".ia-error",
            Map.of(
                "email", "#input-694",
                "phone", "#input-855",
                "name", "#input-412"
            )
        );
    }
}
