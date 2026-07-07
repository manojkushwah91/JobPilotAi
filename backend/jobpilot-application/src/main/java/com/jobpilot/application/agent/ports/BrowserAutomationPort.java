package com.jobpilot.application.agent.ports;

import java.util.Map;

public interface BrowserAutomationPort {

    void launchBrowser();

    void navigateTo(String url);

    void fillField(String selector, String value);

    void clickElement(String selector);

    void uploadFile(String selector, String filePath);

    String getPageContent();

    byte[] takeScreenshot();

    void waitForElement(String selector, int timeoutSeconds);

    void closeBrowser();

    Map<String, Object> getApplicationFormFields(String url);

    default void saveCookiesForPortal(String portal) {}

    default void loadCookiesForPortal(String portal) {}

    default boolean isAvailable() {
        return true;
    }
}
