package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.PageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ActionPlanner {

    private static final Logger log = LoggerFactory.getLogger(ActionPlanner.class);

    private final PlaywrightBrowserManager browserManager;
    private final PlaywrightDomAnalyzer domAnalyzer;

    public ActionPlanner(PlaywrightBrowserManager browserManager, PlaywrightDomAnalyzer domAnalyzer) {
        this.browserManager = browserManager;
        this.domAnalyzer = domAnalyzer;
    }

    public List<PageAction> planLogin(String loginUrl, String usernameSelector, String passwordSelector,
                                       String submitSelector, String username, String password) {
        var actions = new ArrayList<PageAction>();

        actions.add(PageAction.navigate(loginUrl));
        actions.add(PageAction.wait(usernameSelector, 10));
        actions.add(PageAction.fill(usernameSelector, username, "Fill username"));
        actions.add(PageAction.fill(passwordSelector, password, "Fill password"));
        actions.add(PageAction.click(submitSelector, "Click submit"));

        return actions;
    }

    public List<PageAction> planSearch(String searchUrl, Map<String, String> searchParams) {
        var actions = new ArrayList<PageAction>();

        actions.add(PageAction.navigate(searchUrl));

        for (var entry : searchParams.entrySet()) {
            var selector = "[name='" + entry.getKey() + "']";
            if (domAnalyzer.isElementVisible(selector)) {
                actions.add(PageAction.fill(selector, entry.getValue(), "Fill " + entry.getKey()));
            }
        }

        actions.add(PageAction.click("button[type='submit']", "Click search"));

        return actions;
    }

    public List<PageAction> planEasyApply(String jobUrl) {
        var actions = new ArrayList<PageAction>();

        actions.add(PageAction.navigate(jobUrl));
        actions.add(PageAction.wait(".jobs-apply-button", 10));
        actions.add(PageAction.click(".jobs-apply-button", "Click Easy Apply"));

        return actions;
    }

    public List<PageAction> planFormFill(String formSelector, Map<String, String> fieldValues) {
        var actions = new ArrayList<PageAction>();
        var fields = domAnalyzer.analyzeForm(formSelector);

        for (var field : fields) {
            var value = fieldValues.get(field.name());
            if (value != null) {
                actions.add(PageAction.fill(field.selector(), value, "Fill " + field.name()));
            }
        }

        return actions;
    }

    public List<PageAction> planQuestionAnswers(Map<String, String> questionAnswers) {
        var actions = new ArrayList<PageAction>();

        for (var entry : questionAnswers.entrySet()) {
            actions.add(PageAction.fill(entry.getKey(), entry.getValue(), "Answer question"));
        }

        return actions;
    }

    public List<PageAction> planSubmit() {
        var actions = new ArrayList<PageAction>();

        actions.add(PageAction.click("button[type='submit']", "Submit form"));

        return actions;
    }

    public List<PageAction> planErrorRecovery(String errorType) {
        var actions = new ArrayList<PageAction>();

        switch (errorType.toLowerCase()) {
            case "timeout" -> {
                actions.add(PageAction.screenshot(new byte[0]));
                actions.add(PageAction.navigate(browserManager.getPageUrl()));
            }
            case "element_not_found" -> {
                actions.add(PageAction.screenshot(new byte[0]));
            }
            case "navigation_error" -> {
                actions.add(PageAction.screenshot(new byte[0]));
                actions.add(PageAction.navigate(browserManager.getPageUrl()));
            }
            default -> {
                actions.add(PageAction.screenshot(new byte[0]));
            }
        }

        return actions;
    }

    public void executeActions(BrowserSession session, List<PageAction> actions) {
        for (var action : actions) {
            try {
                executeSingleAction(session, action);
                session.recordAction(action);
            } catch (Exception e) {
                log.error("Failed to execute action {}: {}", action.type(), e.getMessage());
                session.fail("Action failed: " + action.type());
                throw new RuntimeException("Action execution failed", e);
            }
        }
    }

    private void executeSingleAction(BrowserSession session, PageAction action) {
        switch (action.type()) {
            case NAVIGATE -> browserManager.navigateTo(action.value());
            case CLICK -> browserManager.click(action.selector());
            case FILL -> browserManager.fill(action.selector(), action.value());
            case SELECT -> browserManager.selectOption(action.selector(), action.value());
            case UPLOAD -> browserManager.uploadFile(action.selector(), action.value());
            case WAIT -> browserManager.waitForSelector(action.selector(), 10000);
            case SCREENSHOT -> session.recordScreenshot(browserManager.takeScreenshot());
            case SCROLL -> browserManager.scrollToElement(action.selector());
            case HOVER -> browserManager.hover(action.selector());
            case KEY_PRESS -> browserManager.pressKey(action.value());
            case EVALUATE_JS -> browserManager.evaluateJavaScript(action.value());
            default -> {}
        }
    }
}
