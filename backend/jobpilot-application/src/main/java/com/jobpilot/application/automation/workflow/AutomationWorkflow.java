package com.jobpilot.application.automation.workflow;

import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.application.automation.ports.PlaywrightAutomationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AutomationWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(AutomationWorkflow.class);

    protected final PlaywrightAutomationPort playwright;
    protected final List<WorkflowStep> steps;

    public AutomationWorkflow(PlaywrightAutomationPort playwright) {
        this.playwright = playwright;
        this.steps = new ArrayList<>();
    }

    public void execute(AutomationSession session) {
        try {
            playwright.startBrowser();
            
            for (int i = 0; i < steps.size(); i++) {
                var step = steps.get(i);
                int progress = (int) ((i + 1) / (double) steps.size() * 100);
                
                session.updateProgress(progress, step.description());
                logger.info("Executing step {}: {}", i + 1, step.description());
                
                try {
                    step.execute(session);
                    session.addScreenshot(playwright.takeScreenshot());
                } catch (Exception e) {
                    logger.error("Step {} failed: {}", step.description(), e.getMessage());
                    throw e;
                }
            }
            
            session.complete();
        } catch (Exception e) {
            logger.error("Automation workflow failed: {}", e.getMessage());
            session.fail(e.getMessage());
        } finally {
            playwright.closeBrowser();
        }
    }

    protected void addStep(WorkflowStep step) {
        steps.add(step);
    }

    protected interface WorkflowStep {
        void execute(AutomationSession session) throws Exception;
        String description();
    }
}
