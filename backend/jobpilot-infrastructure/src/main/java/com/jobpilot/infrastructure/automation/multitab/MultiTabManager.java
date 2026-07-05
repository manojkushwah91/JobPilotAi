package com.jobpilot.infrastructure.automation.multitab;

import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MultiTabManager {

    private static final Logger log = LoggerFactory.getLogger(MultiTabManager.class);

    private final Map<String, Page> tabPages = new ConcurrentHashMap<>();
    private final AtomicInteger tabCounter = new AtomicInteger(0);

    @Value("${jobpilot.multitab.max-tabs:3}")
    private int maxTabs;

    private Semaphore tabSemaphore;

    public void initialize() {
        tabSemaphore = new Semaphore(maxTabs);
    }

    public String openTab(PlaywrightBrowserManager browserManager, String url) {
        try {
            tabSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for tab", e);
        }

        var tabId = "tab-" + tabCounter.incrementAndGet();
        var context = browserManager.getContext();
        var page = context.newPage();

        if (url != null && !url.isEmpty()) {
            page.navigate(url);
        }

        tabPages.put(tabId, page);
        log.info("Opened tab {} for URL: {}", tabId, url);
        return tabId;
    }

    public Page getTab(String tabId) {
        return tabPages.get(tabId);
    }

    public void closeTab(String tabId) {
        var page = tabPages.remove(tabId);
        if (page != null) {
            page.close();
            tabSemaphore.release();
            log.info("Closed tab {}", tabId);
        }
    }

    public void closeAllTabs() {
        tabPages.forEach((id, page) -> {
            try {
                page.close();
            } catch (Exception e) {
                log.warn("Failed to close tab {}: {}", id, e.getMessage());
            }
        });
        tabPages.clear();
        if (tabSemaphore != null) {
            for (int i = 0; i < maxTabs; i++) {
                tabSemaphore.release();
            }
        }
        log.info("Closed all tabs");
    }

    public int getOpenTabCount() {
        return tabPages.size();
    }

    public boolean hasAvailableTabs() {
        return tabSemaphore != null && tabSemaphore.availablePermits() > 0;
    }

    public void switchToTab(String tabId) {
        var page = tabPages.get(tabId);
        if (page != null) {
            page.bringToFront();
        }
    }
}
