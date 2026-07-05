package com.jobpilot.infrastructure.automation.lever;

import com.jobpilot.domain.automation.JobBoardAdapter;
import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LeverAdapter implements JobBoardAdapter {

    private static final Logger log = LoggerFactory.getLogger(LeverAdapter.class);

    private final PlaywrightBrowserManager browserManager;
    private final SessionManager sessionManager;

    public LeverAdapter(PlaywrightBrowserManager browserManager, SessionManager sessionManager) {
        this.browserManager = browserManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public String name() {
        return "Lever";
    }

    @Override
    public String baseUrl() {
        return "https://jobs.lever.co";
    }

    @Override
    public AdapterCapabilities capabilities() {
        return new AdapterCapabilities(
            false,  // supportsEasyApply
            true,   // supportsDirectApply
            false,  // supportsLogin (Lever jobs are company-specific)
            true,   // supportsSearch
            false,  // supportsPagination
            true,   // supportsFilters
            25      // maxResultsPerPage
        );
    }

    @Override
    public LoginFlow loginFlow() {
        return new LoginFlow(
            "",
            "",
            "",
            "",
            List.of(),
            List.of()
        );
    }

    @Override
    public SearchFlow searchFlow() {
        return new SearchFlow(
            "",
            "",
            "",
            ".posting",
            ".posting a.posting-name",
            ".posting .posting-company",
            ".posting .posting-category-primary",
            null,
            null,
            null
        );
    }

    @Override
    public ApplicationFlow applicationFlow() {
        return new ApplicationFlow(
            ".posting-btn-submit",
            ".posting-btn-submit",
            "button:has-text('Next')",
            ".posting-btn-submit",
            null,
            ".application-form",
            "input[type='file']",
            ".application-question",
            ".application-success",
            ".application-error",
            Map.of(
                "name", ".application-name input",
                "email", ".application-email input",
                "phone", ".application-phone input",
                "resume", "input[type='file'][name='resume']",
                "cover_letter", "input[type='file'][name='comments']",
                "linkedin", ".application-linkedin input",
                "website", ".application-urls input"
            )
        );
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }

    @Override
    public int requestDelayMs() {
        return 1500;
    }
}
