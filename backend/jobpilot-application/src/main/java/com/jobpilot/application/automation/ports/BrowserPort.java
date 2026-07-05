package com.jobpilot.application.automation.ports;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface BrowserPort {

    void launch(String browserType, Map<String, Object> options);

    void navigateTo(String url);

    void waitForSelector(String selector, int timeoutMs);

    void click(String selector);

    void fill(String selector, String value);

    void selectOption(String selector, String value);

    void uploadFile(String selector, String filePath);

    void scrollToElement(String selector);

    void hover(String selector);

    void pressKey(String key);

    void evaluateJavaScript(String script);

    String getText(String selector);

    String getAttribute(String selector, String attribute);

    String getPageUrl();

    String getPageTitle();

    String getPageContent();

    byte[] takeScreenshot();

    byte[] takeElementScreenshot(String selector);

    void waitForNavigation(int timeoutMs);

    void waitForLoadState(String state, int timeoutMs);

    void close();

    void closeBrowser();

    boolean isBrowserOpen();

    default CompletableFuture<Void> navigateToAsync(String url) {
        return CompletableFuture.runAsync(() -> navigateTo(url));
    }
}
