package com.jobpilot.domain.automation;

import java.util.List;
import java.util.Map;

public interface JobBoardAdapter {

    String name();

    String baseUrl();

    AdapterCapabilities capabilities();

    LoginFlow loginFlow();

    SearchFlow searchFlow();

    ApplicationFlow applicationFlow();

    default boolean requiresAuthentication() {
        return true;
    }

    default List<String> supportedJobTypes() {
        return List.of("FULL_TIME", "PART_TIME", "CONTRACT");
    }

    default int maxConcurrentSessions() {
        return 3;
    }

    default int requestDelayMs() {
        return 2000;
    }

    record AdapterCapabilities(
        boolean supportsEasyApply,
        boolean supportsDirectApply,
        boolean supportsLogin,
        boolean supportsSearch,
        boolean supportsPagination,
        boolean supportsFilters,
        int maxResultsPerPage
    ) {}

    record LoginFlow(
        String loginUrl,
        String usernameSelector,
        String passwordSelector,
        String submitSelector,
        List<String> postLoginIndicators,
        List<String> captchaIndicators
    ) {}

    record SearchFlow(
        String searchUrl,
        String searchInputSelector,
        String searchButtonSelector,
        String jobCardSelector,
        String jobTitleSelector,
        String companyNameSelector,
        String locationSelector,
        String easyApplyBadgeSelector,
        String paginationSelector,
        String nextPageSelector
    ) {}

    record ApplicationFlow(
        String easyApplyButtonSelector,
        String applyButtonSelector,
        String nextButtonSelector,
        String submitButtonSelector,
        String reviewButtonSelector,
        String formContainerSelector,
        String fileUploadSelector,
        String questionContainerSelector,
        String successIndicatorSelector,
        String errorIndicatorSelector,
        Map<String, String> fieldSelectors
    ) {}
}
