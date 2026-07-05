package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.NavigationEnginePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaywrightNavigationEngine implements NavigationEnginePort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightNavigationEngine.class);

    private final PlaywrightBrowserManager browserManager;

    public PlaywrightNavigationEngine(PlaywrightBrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    @Override
    public void navigateTo(String url) {
        log.debug("Navigating to {}", url);
        browserManager.navigateTo(url);
    }

    @Override
    public void goBack() {
        browserManager.goBack();
    }

    @Override
    public void goForward() {
        browserManager.goForward();
    }

    @Override
    public void reload() {
        browserManager.reload();
    }

    @Override
    public void waitForPageLoad(int timeoutMs) {
        browserManager.waitForLoadState("load", timeoutMs);
    }

    @Override
    public void waitForElement(String selector, int timeoutMs) {
        browserManager.waitForSelector(selector, timeoutMs);
    }

    @Override
    public void scrollUntilVisible(String selector, int maxScrolls) {
        var page = browserManager.getPage();
        for (int i = 0; i < maxScrolls; i++) {
            if (page.querySelector(selector) != null &&
                page.querySelector(selector).isVisible()) {
                return;
            }
            browserManager.evaluateJavaScript("window.scrollBy(0, 500)");
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    @Override
    public void scrollToBottom() {
        browserManager.evaluateJavaScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    @Override
    public void switchToTab(int index) {
        var context = browserManager.getContext();
        var pages = context.pages();
        if (index < pages.size()) {
            pages.get(index).bringToFront();
        }
    }

    @Override
    public void closeTab() {
        browserManager.getPage().close();
    }

    @Override
    public List<String> getOpenTabs() {
        var context = browserManager.getContext();
        return context.pages().stream()
            .map(page -> page.url())
            .toList();
    }
}
