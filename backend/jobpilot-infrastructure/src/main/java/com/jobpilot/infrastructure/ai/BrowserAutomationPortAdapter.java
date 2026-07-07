package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.infrastructure.automation.BrowserAutomationFramework;
import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.jobpilot.infrastructure.automation.PlaywrightDomAnalyzer;
import com.jobpilot.infrastructure.automation.persistence.CookiePersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BrowserAutomationPortAdapter implements BrowserAutomationPort {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutomationPortAdapter.class);

    private final BrowserAutomationFramework framework;
    private final PlaywrightBrowserManager browserManager;
    private final PlaywrightDomAnalyzer domAnalyzer;
    @Autowired(required = false)
    private CookiePersistenceManager cookiePersistenceManager;

    public BrowserAutomationPortAdapter(BrowserAutomationFramework framework,
                                         PlaywrightBrowserManager browserManager,
                                         PlaywrightDomAnalyzer domAnalyzer) {
        this.framework = framework;
        this.browserManager = browserManager;
        this.domAnalyzer = domAnalyzer;
    }

    @Override
    public void launchBrowser() {
        log.info("Launching browser via port adapter");
        framework.initialize("chromium", true, null);
    }

    @Override
    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        var page = browserManager.getPage();
        if (page != null) {
            page.navigate(url);
            page.waitForTimeout(2000);
        }
    }

    @Override
    public void fillField(String selector, String value) {
        log.info("Filling field: {} with value", selector);
        var page = browserManager.getPage();
        if (page != null) {
            page.fill(selector, value);
        }
    }

    @Override
    public void clickElement(String selector) {
        log.info("Clicking element: {}", selector);
        var page = browserManager.getPage();
        if (page != null) {
            page.click(selector);
        }
    }

    @Override
    public void uploadFile(String selector, String filePath) {
        log.info("Uploading file to: {}", selector);
        var page = browserManager.getPage();
        if (page != null) {
            page.setInputFiles(selector, java.nio.file.Paths.get(filePath));
        }
    }

    @Override
    public String getPageContent() {
        var page = browserManager.getPage();
        return page != null ? page.content() : "";
    }

    @Override
    public byte[] takeScreenshot() {
        var page = browserManager.getPage();
        if (page != null) {
            return page.screenshot();
        }
        return new byte[0];
    }

    @Override
    public void waitForElement(String selector, int timeoutSeconds) {
        var page = browserManager.getPage();
        if (page != null) {
            page.waitForSelector(selector,
                new com.microsoft.playwright.Page.WaitForSelectorOptions()
                    .setTimeout(timeoutSeconds * 1000.0));
        }
    }

    @Override
    public void closeBrowser() {
        log.info("Closing browser via port adapter");
        framework.cleanup();
    }

    @Override
    public void saveCookiesForPortal(String portal) {
        if (cookiePersistenceManager != null) {
            cookiePersistenceManager.saveCookies(browserManager, portal);
            log.info("Saved cookies for portal: {}", portal);
        }
    }

    @Override
    public void loadCookiesForPortal(String portal) {
        if (cookiePersistenceManager != null && cookiePersistenceManager.hasCookies(portal)) {
            cookiePersistenceManager.loadCookies(browserManager, portal);
            log.info("Loaded cookies for portal: {}", portal);
        }
    }

    @Override
    public Map<String, Object> getApplicationFormFields(String url) {
        var result = new LinkedHashMap<String, Object>();
        try {
            var page = browserManager.getPage();
            if (page == null) return result;

            var pageType = domAnalyzer.detectPageType();
            result.put("pageType", pageType);

            var forms = page.querySelectorAll("form");
            result.put("formCount", forms.size());

            var allFields = domAnalyzer.analyzeAllForms();
            var fieldsList = new java.util.ArrayList<Map<String, Object>>();
            for (var field : allFields) {
                var fieldMap = new LinkedHashMap<String, Object>();
                fieldMap.put("selector", field.selector());
                fieldMap.put("type", field.type());
                fieldMap.put("label", field.label());
                fieldMap.put("name", field.name());
                fieldMap.put("required", field.required());
                fieldMap.put("value", field.currentValue());
                if (field.options() != null) fieldMap.put("options", field.options());
                fieldsList.add(fieldMap);
            }
            result.put("fields", fieldsList);

            var captchas = domAnalyzer.detectCAPTCHAs();
            result.put("hasCaptcha", !captchas.isEmpty());
            result.put("captchaCount", captchas.size());

            var jobDetails = domAnalyzer.extractJobDetails();
            result.put("jobDetails", jobDetails);

        } catch (Exception e) {
            log.error("Failed to analyze form fields: {}", e.getMessage());
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
