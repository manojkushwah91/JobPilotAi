package com.jobpilot.application.automation.ports;

public interface PlaywrightAutomationPort {
    void startBrowser();
    void navigateTo(String url);
    void fillField(String selector, String value);
    void click(String selector);
    String takeScreenshot();
    void waitForSelector(String selector);
    void waitForTimeout(int millis);
    String getHtml();
    String evaluate(String script);
    String getCurrentUrl();
    void closeBrowser();
}
