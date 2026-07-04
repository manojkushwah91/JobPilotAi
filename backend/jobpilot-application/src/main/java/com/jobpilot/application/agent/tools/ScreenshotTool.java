package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScreenshotTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotTool.class);

    private final BrowserAutomationPort browserAutomation;

    public ScreenshotTool(BrowserAutomationPort browserAutomation) {
        this.browserAutomation = browserAutomation;
    }

    @Override
    public String name() {
        return "TAKE_SCREENSHOT";
    }

    @Override
    public String description() {
        return "Takes a screenshot of the current browser state";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Taking screenshot");

        try {
            var screenshot = browserAutomation.takeScreenshot();
            return Map.of(
                "status", "success",
                "screenshot", screenshot != null ? "captured" : "failed"
            );
        } catch (Exception e) {
            log.error("Screenshot failed: {}", e.getMessage());
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        }
    }

    @Override
    public int timeoutSeconds() {
        return 30;
    }
}
