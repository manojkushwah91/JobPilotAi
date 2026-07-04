package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.PlaywrightAutomationPort;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PlaywrightAutomationAdapter implements PlaywrightAutomationPort, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightAutomationAdapter.class);
    private static final String SCREENSHOT_DIR = "screenshots";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @Override
    public void startBrowser() {
        log.info("Starting Playwright browser...");
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true)
            .setArgs(java.util.List.of("--no-sandbox", "--disable-setuid-sandbox")));
        context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1280, 720)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
        page = context.newPage();
        log.info("Playwright browser started successfully");
    }

    @Override
    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        ensurePage();
        page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
    }

    @Override
    public void fillField(String selector, String value) {
        log.info("Filling {} = {}", selector, value);
        ensurePage();
        page.fill(selector, value);
    }

    @Override
    public void click(String selector) {
        log.info("Clicking: {}", selector);
        ensurePage();
        page.click(selector);
    }

    @Override
    public String takeScreenshot() {
        log.info("Taking screenshot");
        ensurePage();
        var dir = Paths.get(SCREENSHOT_DIR);
        dir.toFile().mkdirs();
        var path = dir.resolve(UUID.randomUUID() + ".png");
        page.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
        return "/" + SCREENSHOT_DIR + "/" + path.getFileName();
    }

    @Override
    public void waitForSelector(String selector) {
        log.info("Waiting for: {}", selector);
        ensurePage();
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    @Override
    public void waitForTimeout(int millis) {
        ensurePage();
        page.waitForTimeout(millis);
    }

    @Override
    public String getHtml() {
        ensurePage();
        return page.content();
    }

    @Override
    public String evaluate(String script) {
        ensurePage();
        var result = page.evaluate(script);
        return result != null ? result.toString() : "";
    }

    @Override
    public String getCurrentUrl() {
        ensurePage();
        return page.url();
    }

    @Override
    public void closeBrowser() {
        log.info("Closing browser");
        if (page != null) { page.close(); page = null; }
        if (context != null) { context.close(); context = null; }
        if (browser != null) { browser.close(); browser = null; }
        if (playwright != null) { playwright.close(); playwright = null; }
    }

    @Override
    public void destroy() {
        closeBrowser();
    }

    private void ensurePage() {
        if (page == null) {
            startBrowser();
        }
    }
}
