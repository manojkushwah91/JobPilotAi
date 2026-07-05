package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.PageAction;
import com.jobpilot.application.automation.ports.*;
import com.jobpilot.domain.automation.JobBoardAdapter;
import com.jobpilot.domain.automation.ApplicationResult;
import com.jobpilot.application.agent.ports.AiProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BrowserAutomationFramework {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutomationFramework.class);

    private final PlaywrightBrowserManager browserManager;
    private final PlaywrightDomAnalyzer domAnalyzer;
    private final PlaywrightFormEngine formEngine;
    private final PlaywrightQuestionDetector questionDetector;
    private final DefaultAIAnswerEngine aiAnswerEngine;
    private final PlaywrightScreenshotManager screenshotManager;
    private final SessionManager sessionManager;
    private final ActionPlanner actionPlanner;
    private final PlaywrightCaptchaDetector captchaDetector;
    private final PlaywrightNavigationEngine navigationEngine;
    private final DefaultRetryManager retryManager;
    private final AutomationErrorHandler errorHandler;
    private final AiProviderPort aiProviderPort;

    public BrowserAutomationFramework(
            PlaywrightBrowserManager browserManager,
            PlaywrightDomAnalyzer domAnalyzer,
            PlaywrightFormEngine formEngine,
            PlaywrightQuestionDetector questionDetector,
            DefaultAIAnswerEngine aiAnswerEngine,
            PlaywrightScreenshotManager screenshotManager,
            SessionManager sessionManager,
            ActionPlanner actionPlanner,
            PlaywrightCaptchaDetector captchaDetector,
            PlaywrightNavigationEngine navigationEngine,
            DefaultRetryManager retryManager,
            AutomationErrorHandler errorHandler,
            AiProviderPort aiProviderPort) {
        this.browserManager = browserManager;
        this.domAnalyzer = domAnalyzer;
        this.formEngine = formEngine;
        this.questionDetector = questionDetector;
        this.aiAnswerEngine = aiAnswerEngine;
        this.screenshotManager = screenshotManager;
        this.sessionManager = sessionManager;
        this.actionPlanner = actionPlanner;
        this.captchaDetector = captchaDetector;
        this.navigationEngine = navigationEngine;
        this.retryManager = retryManager;
        this.errorHandler = errorHandler;
        this.aiProviderPort = aiProviderPort;
    }

    public void initialize(String browserType) {
        log.info("Initializing browser automation framework with {}", browserType);
        browserManager.launch(browserType, Map.of("headless", true));
    }

    public void initialize(String browserType, boolean headless, String proxy) {
        log.info("Initializing browser automation framework with {} (headless={}, proxy={})",
            browserType, headless, proxy != null && !proxy.isEmpty());
        browserManager.launch(browserType, Map.of(
            "headless", headless,
            "proxy", proxy != null ? proxy : ""
        ));
    }

    public BrowserSession startSession(String boardName, Map<String, String> credentials) {
        return sessionManager.createSession(boardName, credentials);
    }

    public ApplicationResult executeApplication(BrowserSession session, JobBoardAdapter adapter,
                                                 Map<String, String> userProfile, String jobUrl) {
        try {
            session.activate();
            log.info("Starting application for job: {}", jobUrl);

            var loginFlow = adapter.loginFlow();
            var loginActions = planLoginActions(loginFlow, userProfile);
            actionPlanner.executeActions(session, loginActions);

            if (captchaDetector.detectCaptcha()) {
                session.pauseForCaptcha();
                return createPendingResult(session, jobUrl, "CAPTCHA detected during login");
            }

            var applicationFlow = adapter.applicationFlow();
            var applyActions = planApplicationActions(applicationFlow, jobUrl);
            actionPlanner.executeActions(session, applyActions);

            var questions = questionDetector.detectQuestions();
            if (!questions.isEmpty()) {
                var answers = answerQuestions(questions, userProfile, jobUrl);
                var answerActions = actionPlanner.planQuestionAnswers(answers);
                actionPlanner.executeActions(session, answerActions);
            }

            var submitActions = actionPlanner.planSubmit();
            actionPlanner.executeActions(session, submitActions);

            session.complete();
            screenshotManager.captureFullPage("application_completed");

            return createSubmittedResult(session, jobUrl);

        } catch (AutomationErrorHandler.CaptchaDetectedException e) {
            return createPendingResult(session, jobUrl, "CAPTCHA requires manual intervention");
        } catch (Exception e) {
            log.error("Application failed: {}", e.getMessage());
            session.fail(e.getMessage());
            screenshotManager.captureFullPage("application_failed");
            return createFailedResult(session, jobUrl, e.getMessage());
        }
    }

    public Map<String, String> answerQuestions(List<QuestionDetectorPort.ApplicationQuestion> questions,
                                                Map<String, String> userProfile, String jobUrl) {
        var answers = new java.util.HashMap<String, String>();

        for (var question : questions) {
            var answer = aiAnswerEngine.generateAnswer(question.text(), jobUrl, userProfile);
            answers.put(question.selector(), answer);
        }

        return answers;
    }

    public PlaywrightBrowserManager getBrowserManager() {
        return browserManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void cleanup() {
        log.info("Cleaning up browser automation framework");
        browserManager.closeBrowser();
    }

    private List<PageAction> planLoginActions(JobBoardAdapter.LoginFlow loginFlow, Map<String, String> credentials) {
        var actions = new java.util.ArrayList<PageAction>();

        actions.add(PageAction.navigate(loginFlow.loginUrl()));
        actions.add(PageAction.wait(loginFlow.usernameSelector(), 10));
        actions.add(PageAction.fill(loginFlow.usernameSelector(), credentials.get("username"), "Fill username"));
        actions.add(PageAction.fill(loginFlow.passwordSelector(), credentials.get("password"), "Fill password"));
        actions.add(PageAction.click(loginFlow.submitSelector(), "Click submit"));
        actions.add(PageAction.wait("", 5));

        return actions;
    }

    private List<PageAction> planApplicationActions(JobBoardAdapter.ApplicationFlow applicationFlow, String jobUrl) {
        var actions = new java.util.ArrayList<PageAction>();

        actions.add(PageAction.navigate(jobUrl));
        actions.add(PageAction.wait(applicationFlow.easyApplyButtonSelector(), 10));
        actions.add(PageAction.click(applicationFlow.easyApplyButtonSelector(), "Click Easy Apply"));
        actions.add(PageAction.wait(applicationFlow.formContainerSelector(), 10));

        return actions;
    }

    private ApplicationResult createPendingResult(BrowserSession session, String jobUrl, String reason) {
        var result = ApplicationResult.create(null, null, null);
        result.setJobUrl(jobUrl);
        result.markRequiresCaptcha();
        return result;
    }

    private ApplicationResult createSubmittedResult(BrowserSession session, String jobUrl) {
        var result = ApplicationResult.create(null, null, null);
        result.setJobUrl(jobUrl);
        result.markSubmitted();
        return result;
    }

    private ApplicationResult createFailedResult(BrowserSession session, String jobUrl, String error) {
        var result = ApplicationResult.create(null, null, null);
        result.setJobUrl(jobUrl);
        result.markFailed(error);
        return result;
    }
}
