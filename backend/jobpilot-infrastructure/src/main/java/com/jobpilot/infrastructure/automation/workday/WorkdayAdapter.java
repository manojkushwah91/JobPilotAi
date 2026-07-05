package com.jobpilot.infrastructure.automation.workday;

import com.jobpilot.domain.automation.JobBoardAdapter;
import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WorkdayAdapter implements JobBoardAdapter {

    private static final Logger log = LoggerFactory.getLogger(WorkdayAdapter.class);

    private final PlaywrightBrowserManager browserManager;
    private final SessionManager sessionManager;

    public WorkdayAdapter(PlaywrightBrowserManager browserManager, SessionManager sessionManager) {
        this.browserManager = browserManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public String name() {
        return "Workday";
    }

    @Override
    public String baseUrl() {
        return "https://www.myworkdayjobs.com";
    }

    @Override
    public AdapterCapabilities capabilities() {
        return new AdapterCapabilities(
            false,  // supportsEasyApply
            true,   // supportsDirectApply
            true,   // supportsLogin (Workday uses SSO per company)
            true,   // supportsSearch
            true,   // supportsPagination
            true,   // supportsFilters
            25      // maxResultsPerPage
        );
    }

    @Override
    public LoginFlow loginFlow() {
        return new LoginFlow(
            "input[data-automation-id='emailAddress'], input[name='email'], input[type='email']",
            "input[data-automation-id='password'], input[name='password'], input[type='password']",
            "button[data-automation-id='signInSubmit'], button[type='submit']",
            "",
            List.of(),
            List.of()
        );
    }

    @Override
    public SearchFlow searchFlow() {
        return new SearchFlow(
            "",
            "input[data-automation-id='searchBar'], input[placeholder*='Search']",
            "",
            "",
            "[data-automation-id='jobResultItem'], .gwt-job-result-item, .WGDJobsListRow",
            "[data-automation-id='jobTitle'], .gwt-job-title a",
            "[data-automation-id='companyName'], .gwt-job-company",
            "[data-automation-id='location'], .gwt-job-location",
            null,
            "button[data-automation-id='nextPage'], .WGDJobsListPagination-next"
        );
    }

    @Override
    public ApplicationFlow applicationFlow() {
        return new ApplicationFlow(
            "button[data-automation-id='applyButton'], a[href*='apply'], button:has-text('Apply')",
            "button[data-automation-id='submit'], button[type='submit']:has-text('Submit')",
            "button[data-automation-id='next'], button:has-text('Next')",
            "button[data-automation-id='submit'], button[type='submit']",
            null,
            "[data-automation-id='multiStepApplicationForm'], form",
            "input[type='file']",
            "[data-automation-id*='formField'], .WGDFormRow, .gwt-Label",
            ".WGDNotification, .gwt-ErrorPanel",
            ".WGDFormError, .gwt-Label[style*='red']",
            Map.of(
                "firstName", "input[data-automation-id='legalNameSection_firstName'], input[name='firstName']",
                "lastName", "input[data-automation-id='legalNameSection_lastName'], input[name='lastName']",
                "email", "input[data-automation-id='email'], input[name='email']",
                "phone", "input[data-automation-id='phone-number'], input[name='phone']",
                "resume", "input[type='file'][data-automation-id='file-upload-input-ref']",
                "cover_letter", "input[type='file'][data-automation-id='file-upload-cover-letter']",
                "location", "input[data-automation-id='location'], input[name='location']"
            )
        );
    }

    @Override
    public boolean requiresAuthentication() {
        return true;
    }

    @Override
    public int requestDelayMs() {
        return 2000;
    }
}
