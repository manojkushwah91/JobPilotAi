package com.jobpilot.application.automation.ports;

import java.util.List;

public interface NavigationEnginePort {

    void navigateTo(String url);

    void goBack();

    void goForward();

    void reload();

    void waitForPageLoad(int timeoutMs);

    void waitForElement(String selector, int timeoutMs);

    void scrollUntilVisible(String selector, int maxScrolls);

    void scrollToBottom();

    void switchToTab(int index);

    void closeTab();

    List<String> getOpenTabs();

    default void navigateWithRetry(String url, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                navigateTo(url);
                return;
            } catch (Exception e) {
                if (i == maxRetries - 1) throw e;
                try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }
}
