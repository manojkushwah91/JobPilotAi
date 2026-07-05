package com.jobpilot.infrastructure.ai;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.infrastructure.automation.BrowserAutomationFramework;
import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BrowserAutomationPortAdapter implements BrowserAutomationPort {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutomationPortAdapter.class);

    private final BrowserAutomationFramework framework;
    private final PlaywrightBrowserManager browserManager;

    public BrowserAutomationPortAdapter(BrowserAutomationFramework framework,
                                         PlaywrightBrowserManager browserManager) {
        this.framework = framework;
        this.browserManager = browserManager;
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
    public Map<String, Object> getApplicationFormFields(String url) {
        return Map.of("fields", Map.of());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
