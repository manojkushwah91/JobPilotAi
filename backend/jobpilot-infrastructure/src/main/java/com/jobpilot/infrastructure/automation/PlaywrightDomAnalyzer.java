package com.jobpilot.infrastructure.automation;

import com.microsoft.playwright.Page;
import com.jobpilot.application.automation.ports.DomAnalyzerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlaywrightDomAnalyzer implements DomAnalyzerPort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightDomAnalyzer.class);

    private final PlaywrightBrowserManager browserManager;

    public PlaywrightDomAnalyzer(PlaywrightBrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    @Override
    public List<FormField> analyzeForm(String formSelector) {
        var page = browserManager.getPage();
        var fields = new ArrayList<FormField>();

        var form = page.querySelector(formSelector);
        if (form == null) return fields;

        var inputs = form.querySelectorAll("input, select, textarea");
        for (var input : inputs) {
            var selector = buildSelector(input);
            var type = input.getAttribute("type") != null ? input.getAttribute("type") : "text";
            var name = input.getAttribute("name") != null ? input.getAttribute("name") : "";
            var label = findLabelForInput(page, input);
            var required = input.getAttribute("required") != null;
            var value = input.inputValue() != null ? input.inputValue() : "";

            List<String> options = null;
            if ("select".equals(input.evaluate("el => el.tagName.toLowerCase()"))) {
                options = new ArrayList<>();
                var optionElements = input.querySelectorAll("option");
                for (var option : optionElements) {
                    options.add(option.innerText());
                }
            }

            fields.add(new FormField(selector, type, label, name, required, value, options, Map.of()));
        }

        return fields;
    }

    @Override
    public List<FormField> analyzeAllForms() {
        var allFields = new ArrayList<FormField>();
        var page = browserManager.getPage();
        var forms = page.querySelectorAll("form");

        for (var form : forms) {
            var formSelector = form.getAttribute("id") != null
                ? "#" + form.getAttribute("id")
                : "form:nth-of-type(" + (forms.size() > 0 ? 1 : 1) + ")";
            allFields.addAll(analyzeForm(formSelector));
        }

        return allFields;
    }

    @Override
    public String detectPageType() {
        var page = browserManager.getPage();
        var url = page.url().toLowerCase();
        var content = page.content().toLowerCase();

        if (url.contains("login") || url.contains("signin") || content.contains("sign in")) {
            return "LOGIN";
        }
        if (url.contains("search") || url.contains("jobs") || content.contains("search results")) {
            return "SEARCH_RESULTS";
        }
        if (url.contains("apply") || content.contains("easy apply") || content.contains("apply now")) {
            return "APPLICATION_FORM";
        }
        if (url.contains("job") || content.contains("job description")) {
            return "JOB_LISTING";
        }
        if (content.contains("captcha") || content.contains("verify you are human")) {
            return "CAPTCHA";
        }
        return "UNKNOWN";
    }

    @Override
    public Map<String, String> extractJobDetails() {
        var page = browserManager.getPage();
        var details = new HashMap<String, String>();

        var titleSelectors = List.of("h1", ".job-title", "[data-test='job-title']", ".job-details__job-title");
        for (var selector : titleSelectors) {
            var element = page.querySelector(selector);
            if (element != null) {
                details.put("title", element.innerText().trim());
                break;
            }
        }

        var companySelectors = List.of(".company-name", "[data-test='company-name']", ".job-details__company");
        for (var selector : companySelectors) {
            var element = page.querySelector(selector);
            if (element != null) {
                details.put("company", element.innerText().trim());
                break;
            }
        }

        var locationSelectors = List.of(".location", "[data-test='job-location']", ".job-details__location");
        for (var selector : locationSelectors) {
            var element = page.querySelector(selector);
            if (element != null) {
                details.put("location", element.innerText().trim());
                break;
            }
        }

        details.put("url", page.url());
        return details;
    }

    @Override
    public List<String> detectCAPTCHAs() {
        var page = browserManager.getPage();
        var captchaSelectors = new ArrayList<String>();

        var captchaIndicators = List.of(
            "iframe[src*='recaptcha']",
            "iframe[src*='hcaptcha']",
            ".g-recaptcha",
            "#captcha",
            "[data-testid='captcha']",
            ".captcha-container",
            "iframe[src*='funcaptcha']"
        );

        for (var selector : captchaIndicators) {
            if (page.querySelector(selector) != null) {
                captchaSelectors.add(selector);
            }
        }

        return captchaSelectors;
    }

    @Override
    public boolean isElementVisible(String selector) {
        var element = browserManager.getPage().querySelector(selector);
        return element != null && element.isVisible();
    }

    @Override
    public boolean isElementEnabled(String selector) {
        var element = browserManager.getPage().querySelector(selector);
        return element != null && element.isEnabled();
    }

    @Override
    public int countElements(String selector) {
        return browserManager.getPage().querySelectorAll(selector).size();
    }

    @Override
    public String getMatchingSelector(List<String> candidates) {
        var page = browserManager.getPage();
        for (var selector : candidates) {
            if (page.querySelector(selector) != null) {
                return selector;
            }
        }
        return null;
    }

    private String buildSelector(com.microsoft.playwright.ElementHandle element) {
        var id = element.getAttribute("id");
        if (id != null && !id.isEmpty()) return "#" + id;

        var name = element.getAttribute("name");
        if (name != null && !name.isEmpty()) return "[name='" + name + "']";

        var testId = element.getAttribute("data-testid");
        if (testId != null && !testId.isEmpty()) return "[data-testid='" + testId + "']";

        return "input[name='" + (name != null ? name : "unknown") + "']";
    }

    private String findLabelForInput(Page page, com.microsoft.playwright.ElementHandle input) {
        var id = input.getAttribute("id");
        if (id != null) {
            var label = page.querySelector("label[for='" + id + "']");
            if (label != null) return label.innerText().trim();
        }

        var parent = input.evaluate("el => el.closest('label')");
        if (parent != null) {
            return page.evaluate("el => el.innerText", parent).toString().trim();
        }

        return "";
    }
}
