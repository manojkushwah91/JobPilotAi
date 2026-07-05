package com.jobpilot.application.agent.tools;

import com.jobpilot.domain.agent.Tool;
import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BrowserAutomationTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutomationTool.class);

    private final BrowserAutomationPort browserAutomation;

    public BrowserAutomationTool(BrowserAutomationPort browserAutomation) {
        this.browserAutomation = browserAutomation;
    }

    @Override
    public String name() {
        return "BROWSER_AUTOMATION";
    }

    @Override
    public String description() {
        return "Executes browser automation for job applications using Playwright";
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing browser automation tool");

        var action = (String) input.getOrDefault("action", "launch");
        var url = (String) input.getOrDefault("url", "");
        var selector = (String) input.getOrDefault("selector", "");
        var value = (String) input.getOrDefault("value", "");
        var filePath = (String) input.getOrDefault("filePath", "");

        try {
            return switch (action.toLowerCase()) {
                case "launch" -> {
                    browserAutomation.launchBrowser();
                    yield Map.of("status", "success", "message", "Browser launched");
                }
                case "navigate" -> {
                    browserAutomation.navigateTo(url);
                    yield Map.of("status", "success", "url", url);
                }
                case "fill" -> {
                    browserAutomation.fillField(selector, value);
                    yield Map.of("status", "success", "selector", selector);
                }
                case "click" -> {
                    browserAutomation.clickElement(selector);
                    yield Map.of("status", "success", "selector", selector);
                }
                case "upload" -> {
                    browserAutomation.uploadFile(selector, filePath);
                    yield Map.of("status", "success", "filePath", filePath);
                }
                case "screenshot" -> {
                    var screenshot = browserAutomation.takeScreenshot();
                    yield Map.of("status", "success", "screenshot", screenshot != null ? "captured" : "failed");
                }
                case "wait" -> {
                    browserAutomation.waitForElement(selector, 10);
                    yield Map.of("status", "success", "selector", selector);
                }
                case "content" -> {
                    var content = browserAutomation.getPageContent();
                    yield Map.of("status", "success", "contentLength", content != null ? content.length() : 0);
                }
                case "form_fields" -> {
                    var fields = browserAutomation.getApplicationFormFields(url);
                    yield Map.of("status", "success", "fields", fields);
                }
                case "close" -> {
                    browserAutomation.closeBrowser();
                    yield Map.of("status", "success", "message", "Browser closed");
                }
                default -> Map.of("status", "error", "message", "Unknown action: " + action);
            };
        } catch (Exception e) {
            log.error("Browser automation failed: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @Override
    public int timeoutSeconds() {
        return 300;
    }
}
