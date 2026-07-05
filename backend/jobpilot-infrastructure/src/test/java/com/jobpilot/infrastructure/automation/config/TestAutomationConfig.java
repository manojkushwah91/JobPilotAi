package com.jobpilot.infrastructure.automation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.jobpilot.application.automation.ports.BrowserPort;
import com.jobpilot.application.automation.ports.DomAnalyzerPort;
import com.jobpilot.application.automation.ports.FormEnginePort;
import com.jobpilot.application.automation.ports.QuestionDetectorPort;
import com.jobpilot.application.automation.ports.ScreenshotManagerPort;
import com.jobpilot.application.automation.ports.NavigationEnginePort;
import com.jobpilot.application.automation.ports.RetryManagerPort;
import com.jobpilot.application.automation.ports.CaptchaDetectorPort;

import java.util.List;
import java.util.Map;

@TestConfiguration
@Profile("test")
public class TestAutomationConfig {

    @Bean
    @Primary
    public BrowserPort testBrowserPort() {
        return new BrowserPort() {
            @Override public void launch(String browserType, Map<String, Object> options) {}
            @Override public void navigateTo(String url) {}
            @Override public void waitForSelector(String selector, int timeoutMs) {}
            @Override public void click(String selector) {}
            @Override public void fill(String selector, String value) {}
            @Override public void selectOption(String selector, String value) {}
            @Override public void uploadFile(String selector, String filePath) {}
            @Override public void scrollToElement(String selector) {}
            @Override public void hover(String selector) {}
            @Override public void pressKey(String key) {}
            @Override public void evaluateJavaScript(String script) {}
            @Override public String getText(String selector) { return "test"; }
            @Override public String getAttribute(String selector, String attribute) { return "test"; }
            @Override public String getPageUrl() { return "https://test.com"; }
            @Override public String getPageTitle() { return "Test"; }
            @Override public String getPageContent() { return "test content"; }
            @Override public byte[] takeScreenshot() { return new byte[0]; }
            @Override public byte[] takeElementScreenshot(String selector) { return new byte[0]; }
            @Override public void waitForNavigation(int timeoutMs) {}
            @Override public void waitForLoadState(String state, int timeoutMs) {}
            @Override public void close() {}
            @Override public void closeBrowser() {}
            @Override public boolean isBrowserOpen() { return false; }
        };
    }

    @Bean
    @Primary
    public DomAnalyzerPort testDomAnalyzerPort() {
        return new DomAnalyzerPort() {
            @Override public List<FormField> analyzeForm(String formSelector) { return List.of(); }
            @Override public List<FormField> analyzeAllForms() { return List.of(); }
            @Override public String detectPageType() { return "UNKNOWN"; }
            @Override public Map<String, String> extractJobDetails() { return Map.of(); }
            @Override public List<String> detectCAPTCHAs() { return List.of(); }
            @Override public boolean isElementVisible(String selector) { return false; }
            @Override public boolean isElementEnabled(String selector) { return false; }
            @Override public int countElements(String selector) { return 0; }
            @Override public String getMatchingSelector(List<String> candidates) { return null; }
        };
    }

    @Bean
    @Primary
    public FormEnginePort testFormEnginePort() {
        return new FormEnginePort() {
            @Override public boolean fillField(String selector, String value, String fieldType) { return true; }
            @Override public boolean selectDropdown(String selector, String value) { return true; }
            @Override public boolean uploadFile(String selector, String filePath) { return true; }
            @Override public boolean answerQuestion(String questionSelector, String answer, String questionType) { return true; }
            @Override public boolean handleCheckbox(String selector, boolean checked) { return true; }
            @Override public boolean handleRadio(String selector, String value) { return true; }
            @Override public Map<String, Boolean> validateForm(String formSelector) { return Map.of(); }
            @Override public List<String> getUnfilledRequiredFields(String formSelector) { return List.of(); }
            @Override public boolean submitForm(String formSelector) { return true; }
        };
    }

    @Bean
    @Primary
    public QuestionDetectorPort testQuestionDetectorPort() {
        return new QuestionDetectorPort() {
            @Override public List<ApplicationQuestion> detectQuestions() { return List.of(); }
            @Override public ApplicationQuestion detectQuestion(String selector) { return null; }
            @Override public boolean hasQuestions() { return false; }
            @Override public int questionCount() { return 0; }
        };
    }

    @Bean
    @Primary
    public ScreenshotManagerPort testScreenshotManagerPort() {
        return new ScreenshotManagerPort() {
            @Override public byte[] takeScreenshot() { return new byte[0]; }
            @Override public byte[] takeElementScreenshot(String selector) { return new byte[0]; }
            @Override public byte[] takeFullPageScreenshot() { return new byte[0]; }
            @Override public String saveScreenshot(byte[] data, String name) { return "test.png"; }
            @Override public List<String> getScreenshotHistory(String sessionId) { return List.of(); }
        };
    }

    @Bean
    @Primary
    public NavigationEnginePort testNavigationEnginePort() {
        return new NavigationEnginePort() {
            @Override public void navigateTo(String url) {}
            @Override public void goBack() {}
            @Override public void goForward() {}
            @Override public void reload() {}
            @Override public void waitForPageLoad(int timeoutMs) {}
            @Override public void waitForElement(String selector, int timeoutMs) {}
            @Override public void scrollUntilVisible(String selector, int maxScrolls) {}
            @Override public void scrollToBottom() {}
            @Override public void switchToTab(int index) {}
            @Override public void closeTab() {}
            @Override public List<String> getOpenTabs() { return List.of(); }
        };
    }

    @Bean
    @Primary
    public RetryManagerPort testRetryManagerPort() {
        return new RetryManagerPort() {
            @Override
            public <T> T executeWithRetry(java.util.function.Supplier<T> action, String actionName, int maxRetries, int delayMs) {
                return action.get();
            }
            @Override public boolean shouldRetry(Exception error, int attemptCount) { return false; }
            @Override public int calculateDelay(int attemptCount, int baseDelayMs) { return baseDelayMs; }
        };
    }

    @Bean
    @Primary
    public CaptchaDetectorPort testCaptchaDetectorPort() {
        return new CaptchaDetectorPort() {
            @Override public boolean detectCaptcha() { return false; }
            @Override public CaptchaType detectCaptchaType() { return CaptchaType.NONE; }
            @Override public List<String> getCaptchaSelectors() { return List.of(); }
            @Override public boolean isCaptchaPresent(String selector) { return false; }
        };
    }
}
