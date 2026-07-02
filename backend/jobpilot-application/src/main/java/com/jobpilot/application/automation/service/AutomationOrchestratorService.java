package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.ports.AutomationRepository;
import com.jobpilot.application.automation.ports.PlaywrightAutomationPort;
import com.jobpilot.domain.automation.AutomationSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AutomationOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AutomationOrchestratorService.class);

    private final AutomationRepository automationRepository;
    private final PlaywrightAutomationPort playwright;

    public AutomationOrchestratorService(AutomationRepository automationRepository, PlaywrightAutomationPort playwright) {
        this.automationRepository = automationRepository;
        this.playwright = playwright;
    }

    public void execute(AutomationSession session) {
        try {
            session.updateProgress(5, "Starting browser");
            automationRepository.save(session);
            playwright.startBrowser();

            session.updateProgress(10, "Navigating to application URL");
            automationRepository.save(session);
            playwright.navigateTo("https://example.com/apply");

            playwright.waitForSelector("input[name='name']");
            session.updateProgress(30, "Filling form");
            automationRepository.save(session);
            playwright.fillField("input[name='name']", "John Doe");
            playwright.fillField("input[name='email']", "john@example.com");
            session.addScreenshot(playwright.takeScreenshot());

            session.updateProgress(50, "Uploading resume");
            automationRepository.save(session);
            playwright.click("input[type='file']");
            session.addScreenshot(playwright.takeScreenshot());

            session.updateProgress(70, "Reviewing application");
            automationRepository.save(session);
            playwright.waitForSelector("button[type='submit']");
            session.addScreenshot(playwright.takeScreenshot());

            session.updateProgress(85, "Awaiting confirmation");
            session.requestConfirmation();
            automationRepository.save(session);
        } catch (Exception e) {
            log.error("Automation failed for session {}: {}", session.sessionId().value(), e.getMessage(), e);
            session.fail(e.getMessage());
            automationRepository.save(session);
        }
    }

    public void confirmAndSubmit(AutomationSession session) {
        try {
            session.confirm();
            session.updateProgress(90, "Submitting application");
            automationRepository.save(session);
            playwright.click("button[type='submit']");
            playwright.waitForSelector(".confirmation-message");
            session.addScreenshot(playwright.takeScreenshot());
            session.updateProgress(100, "Completed");
            session.complete();
            automationRepository.save(session);
        } catch (Exception e) {
            log.error("Automation confirmation failed for session {}: {}", session.sessionId().value(), e.getMessage(), e);
            session.fail(e.getMessage());
            automationRepository.save(session);
        } finally {
            playwright.closeBrowser();
        }
    }

    public void cancel(AutomationSession session) {
        session.cancel();
        automationRepository.save(session);
        playwright.closeBrowser();
    }
}
