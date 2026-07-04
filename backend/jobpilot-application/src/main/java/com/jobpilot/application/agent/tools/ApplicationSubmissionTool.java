package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApplicationSubmissionTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ApplicationSubmissionTool.class);

    private final BrowserAutomationPort browserAutomation;

    public ApplicationSubmissionTool(BrowserAutomationPort browserAutomation) {
        this.browserAutomation = browserAutomation;
    }

    @Override
    public String name() {
        return "SUBMIT_APPLICATION";
    }

    @Override
    public String description() {
        return "Submits a job application using browser automation";
    }

    @Override
    public boolean requiresApproval() {
        return true;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing application submission tool");

        var url = (String) input.getOrDefault("url", "");
        var resumePath = (String) input.getOrDefault("resumePath", "");
        var coverLetter = (String) input.getOrDefault("coverLetter", "");

        try {
            browserAutomation.launchBrowser();
            browserAutomation.navigateTo(url);

            var screenshot = browserAutomation.takeScreenshot();

            return Map.of(
                "status", "awaiting_approval",
                "url", url,
                "screenshot", screenshot != null ? "captured" : "failed",
                "message", "Application prepared. Awaiting user approval."
            );
        } catch (Exception e) {
            log.error("Application submission failed: {}", e.getMessage());
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        } finally {
            try {
                browserAutomation.closeBrowser();
            } catch (Exception e) {
                log.warn("Failed to close browser: {}", e.getMessage());
            }
        }
    }

    @Override
    public int timeoutSeconds() {
        return 120;
    }
}
