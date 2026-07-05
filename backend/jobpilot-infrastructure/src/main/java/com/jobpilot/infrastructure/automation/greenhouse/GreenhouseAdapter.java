package com.jobpilot.infrastructure.automation.greenhouse;

import com.jobpilot.domain.automation.JobBoardAdapter;
import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GreenhouseAdapter implements JobBoardAdapter {

    private static final Logger log = LoggerFactory.getLogger(GreenhouseAdapter.class);

    private final PlaywrightBrowserManager browserManager;
    private final SessionManager sessionManager;

    public GreenhouseAdapter(PlaywrightBrowserManager browserManager, SessionManager sessionManager) {
        this.browserManager = browserManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public String name() {
        return "Greenhouse";
    }

    @Override
    public String baseUrl() {
        return "https://boards.greenhouse.io";
    }

    @Override
    public AdapterCapabilities capabilities() {
        return new AdapterCapabilities(
            false,  // supportsEasyApply (Greenhouse doesn't have Easy Apply)
            true,   // supportsDirectApply
            false,  // supportsLogin (Greenhouse jobs are company-specific, no central login)
            true,   // supportsSearch
            false,  // supportsPagination
            true,   // supportsFilters
            20      // maxResultsPerPage
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
            ".opening",
            ".opening h4",
            ".opening .company",
            ".location",
            null,
            null,
            null
        );
    }

    @Override
    public ApplicationFlow applicationFlow() {
        return new ApplicationFlow(
            "a[href*='apply']",
            "#submit_app",
            "button:has-text('Next')",
            "#submit_app",
            null,
            "#application_form",
            "input[type='file']",
            ".field",
            ".flash",
            ".error",
            Map.of(
                "first_name", "#first_name",
                "last_name", "#last_name",
                "email", "#email",
                "phone", "#phone",
                "resume", "input[type='file'][name='resume']",
                "cover_letter", "input[type='file'][name='cover_letter']",
                "linkedin", "#url",
                "website", "#website"
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
