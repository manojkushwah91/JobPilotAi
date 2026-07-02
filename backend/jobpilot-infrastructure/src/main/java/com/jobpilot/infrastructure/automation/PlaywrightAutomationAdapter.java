package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.PlaywrightAutomationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlaywrightAutomationAdapter implements PlaywrightAutomationPort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightAutomationAdapter.class);

    @Override
    public void startBrowser() {
        log.info("Starting Playwright browser...");
    }

    @Override
    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
    }

    @Override
    public void fillField(String selector, String value) {
        log.info("Filling {} = {}", selector, value);
    }

    @Override
    public void click(String selector) {
        log.info("Clicking: {}", selector);
    }

    @Override
    public String takeScreenshot() {
        log.info("Taking screenshot");
        return "/screenshots/" + UUID.randomUUID() + ".png";
    }

    @Override
    public void waitForSelector(String selector) {
        log.info("Waiting for: {}", selector);
    }

    @Override
    public void closeBrowser() {
        log.info("Closing browser");
    }
}
