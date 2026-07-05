package com.jobpilot.infrastructure.automation;

import com.microsoft.playwright.*;
import com.jobpilot.application.automation.ports.BrowserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlaywrightBrowserManager implements BrowserPort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightBrowserManager.class);

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private final AtomicBoolean isOpen = new AtomicBoolean(false);

    @Override
    public void launch(String browserType, Map<String, Object> options) {
        log.info("Launching {} browser", browserType);
        playwright = Playwright.create();

        var launchOptions = new BrowserType.LaunchOptions()
            .setHeadless((Boolean) options.getOrDefault("headless", true));

        browser = switch (browserType.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(launchOptions);
            case "webkit" -> playwright.webkit().launch(launchOptions);
            default -> playwright.chromium().launch(launchOptions);
        };

        var contextOptions = new Browser.NewContextOptions()
            .setViewportSize(1920, 1080)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        context = browser.newContext(contextOptions);
        page = context.newPage();
        isOpen.set(true);
        log.info("Browser launched successfully");
    }

    @Override
    public void navigateTo(String url) {
        log.debug("Navigating to {}", url);
        page.navigate(url);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    @Override
    public void waitForSelector(String selector, int timeoutMs) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
    }

    @Override
    public void click(String selector) {
        log.debug("Clicking {}", selector);
        page.click(selector);
    }

    @Override
    public void fill(String selector, String value) {
        log.debug("Filling {} with value", selector);
        page.fill(selector, value);
    }

    @Override
    public void selectOption(String selector, String value) {
        page.selectOption(selector, value);
    }

    @Override
    public void uploadFile(String selector, String filePath) {
        log.debug("Uploading file to {}", selector);
        page.setInputFiles(selector, java.nio.file.Paths.get(filePath));
    }

    @Override
    public void scrollToElement(String selector) {
        page.evaluate("el => el.scrollIntoView({behavior: 'smooth', block: 'center'})",
            page.querySelector(selector));
    }

    @Override
    public void hover(String selector) {
        page.hover(selector);
    }

    @Override
    public void pressKey(String key) {
        page.keyboard().press(key);
    }

    @Override
    public void evaluateJavaScript(String script) {
        page.evaluate(script);
    }

    @Override
    public String getText(String selector) {
        var element = page.querySelector(selector);
        return element != null ? element.innerText() : null;
    }

    @Override
    public String getAttribute(String selector, String attribute) {
        var element = page.querySelector(selector);
        return element != null ? element.getAttribute(attribute) : null;
    }

    @Override
    public String getPageUrl() {
        return page.url();
    }

    @Override
    public String getPageTitle() {
        return page.title();
    }

    @Override
    public String getPageContent() {
        return page.content();
    }

    @Override
    public byte[] takeScreenshot() {
        return page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
    }

    @Override
    public byte[] takeElementScreenshot(String selector) {
        var element = page.querySelector(selector);
        if (element != null) {
            return element.screenshot();
        }
        return takeScreenshot();
    }

    @Override
    public void waitForNavigation(int timeoutMs) {
        page.waitForTimeout(timeoutMs);
    }

    @Override
    public void waitForLoadState(String state, int timeoutMs) {
        var loadState = switch (state.toLowerCase()) {
            case "load" -> com.microsoft.playwright.options.LoadState.LOAD;
            case "domcontentloaded" -> com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
            case "networkidle" -> com.microsoft.playwright.options.LoadState.NETWORKIDLE;
            default -> com.microsoft.playwright.options.LoadState.LOAD;
        };
        page.waitForLoadState(loadState, new Page.WaitForLoadStateOptions().setTimeout(timeoutMs));
    }

    @Override
    public void close() {
        if (page != null) page.close();
        if (context != null) context.close();
        isOpen.set(false);
    }

    public void goBack() {
        page.goBack();
    }

    public void goForward() {
        page.goForward();
    }

    public void reload() {
        page.reload();
    }

    public void scrollToBottom() {
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
    }

    @Override
    public void closeBrowser() {
        close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        log.info("Browser closed");
    }

    @Override
    public boolean isBrowserOpen() {
        return isOpen.get();
    }

    public Page getPage() {
        return page;
    }

    public BrowserContext getContext() {
        return context;
    }
}
