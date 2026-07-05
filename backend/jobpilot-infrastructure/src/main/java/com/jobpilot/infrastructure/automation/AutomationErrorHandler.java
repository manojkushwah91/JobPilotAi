package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.PageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class AutomationErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(AutomationErrorHandler.class);

    private final SessionManager sessionManager;
    private final ActionPlanner actionPlanner;
    private final PlaywrightScreenshotManager screenshotManager;

    public AutomationErrorHandler(SessionManager sessionManager, ActionPlanner actionPlanner,
                                  PlaywrightScreenshotManager screenshotManager) {
        this.sessionManager = sessionManager;
        this.actionPlanner = actionPlanner;
        this.screenshotManager = screenshotManager;
    }

    public <T> T executeWithRecovery(BrowserSession session, Supplier<T> action, String actionName) {
        try {
            return action.get();
        } catch (Exception e) {
            log.error("Error executing {}: {}", actionName, e.getMessage());
            return handleRecovery(session, e, actionName, action);
        }
    }

    private <T> T handleRecovery(BrowserSession session, Exception error, String actionName, Supplier<T> action) {
        var errorType = classifyError(error);

        switch (errorType) {
            case "captcha" -> {
                session.pauseForCaptcha();
                log.warn("CAPTCHA detected - session {} paused for manual intervention", session.getId().value());
                throw new CaptchaDetectedException("CAPTCHA detected during " + actionName, error);
            }
            case "login_required" -> {
                session.fail("Login required - session expired");
                throw new SessionExpiredException("Session expired during " + actionName, error);
            }
            case "rate_limit" -> {
                log.warn("Rate limit detected - waiting 60 seconds");
                try { Thread.sleep(60000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return executeWithRecovery(session, action, actionName);
            }
            case "element_not_found" -> {
                log.warn("Element not found during {} - taking screenshot and retrying", actionName);
                screenshotManager.captureFullPage("error_" + actionName);
                session.incrementRetries();
                if (session.hasExceededMaxRetries(3)) {
                    session.fail("Max retries exceeded for " + actionName);
                    throw new MaxRetriesExceededException("Max retries exceeded", error);
                }
                return executeWithRecovery(session, action, actionName);
            }
            default -> {
                session.fail("Unrecoverable error: " + error.getMessage());
                throw new AutomationException("Unrecoverable error during " + actionName, error);
            }
        }
    }

    private String classifyError(Exception error) {
        var message = error.getMessage() != null ? error.getMessage().toLowerCase() : "";
        var cause = error.getCause();
        var causeMessage = cause != null && cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";

        if (message.contains("captcha") || causeMessage.contains("captcha")) return "captcha";
        if (message.contains("login") || message.contains("sign in") || message.contains("session expired")) return "login_required";
        if (message.contains("rate limit") || message.contains("too many requests")) return "rate_limit";
        if (message.contains("element not found") || message.contains("no such element") ||
            message.contains("selector")) return "element_not_found";
        if (message.contains("timeout") || message.contains("timeout")) return "timeout";

        return "unknown";
    }

    public static class CaptchaDetectedException extends RuntimeException {
        public CaptchaDetectedException(String message, Throwable cause) { super(message, cause); }
    }

    public static class SessionExpiredException extends RuntimeException {
        public SessionExpiredException(String message, Throwable cause) { super(message, cause); }
    }

    public static class MaxRetriesExceededException extends RuntimeException {
        public MaxRetriesExceededException(String message, Throwable cause) { super(message, cause); }
    }

    public static class AutomationException extends RuntimeException {
        public AutomationException(String message, Throwable cause) { super(message, cause); }
    }
}
