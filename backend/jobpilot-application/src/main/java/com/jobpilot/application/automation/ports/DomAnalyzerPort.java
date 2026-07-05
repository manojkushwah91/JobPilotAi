package com.jobpilot.application.automation.ports;

import java.util.List;
import java.util.Map;

public interface DomAnalyzerPort {

    List<FormField> analyzeForm(String formSelector);

    List<FormField> analyzeAllForms();

    String detectPageType();

    Map<String, String> extractJobDetails();

    List<String> detectCAPTCHAs();

    boolean isElementVisible(String selector);

    boolean isElementEnabled(String selector);

    int countElements(String selector);

    String getMatchingSelector(List<String> candidates);

    record FormField(
        String selector,
        String type,
        String label,
        String name,
        boolean required,
        String currentValue,
        List<String> options,
        Map<String, String> attributes
    ) {}

    record PageInfo(
        String type,
        String url,
        String title,
        boolean hasLoginForm,
        boolean hasSearchForm,
        boolean hasApplicationForm,
        boolean hasEasyApply,
        boolean hasCAPTCHA,
        int formCount,
        Map<String, Object> metadata
    ) {}
}
