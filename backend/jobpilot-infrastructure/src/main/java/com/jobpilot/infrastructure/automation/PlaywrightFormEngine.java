package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.FormEnginePort;
import com.jobpilot.application.automation.ports.DomAnalyzerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlaywrightFormEngine implements FormEnginePort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightFormEngine.class);

    private final PlaywrightBrowserManager browserManager;
    private final PlaywrightDomAnalyzer domAnalyzer;

    public PlaywrightFormEngine(PlaywrightBrowserManager browserManager, PlaywrightDomAnalyzer domAnalyzer) {
        this.browserManager = browserManager;
        this.domAnalyzer = domAnalyzer;
    }

    @Override
    public boolean fillField(String selector, String value, String fieldType) {
        try {
            var page = browserManager.getPage();
            var element = page.querySelector(selector);
            if (element == null) {
                log.warn("Element not found: {}", selector);
                return false;
            }

            switch (fieldType.toLowerCase()) {
                case "text", "email", "phone", "number", "password" -> {
                    page.fill(selector, value);
                }
                case "textarea" -> {
                    page.fill(selector, value);
                }
                case "select" -> {
                    page.selectOption(selector, value);
                }
                case "checkbox" -> {
                    var checked = Boolean.parseBoolean(value);
                    if (checked && !element.isChecked()) {
                        page.check(selector);
                    } else if (!checked && element.isChecked()) {
                        page.uncheck(selector);
                    }
                }
                case "radio" -> {
                    page.check(selector);
                }
                default -> {
                    page.fill(selector, value);
                }
            }

            log.debug("Filled field {} with value", selector);
            return true;
        } catch (Exception e) {
            log.error("Failed to fill field {}: {}", selector, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean selectDropdown(String selector, String value) {
        try {
            browserManager.getPage().selectOption(selector, value);
            return true;
        } catch (Exception e) {
            log.error("Failed to select dropdown {}: {}", selector, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean uploadFile(String selector, String filePath) {
        try {
            browserManager.uploadFile(selector, filePath);
            return true;
        } catch (Exception e) {
            log.error("Failed to upload file to {}: {}", selector, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean answerQuestion(String questionSelector, String answer, String questionType) {
        return fillField(questionSelector, answer, questionType);
    }

    @Override
    public boolean handleCheckbox(String selector, boolean checked) {
        try {
            var page = browserManager.getPage();
            var element = page.querySelector(selector);
            if (element == null) return false;

            if (checked && !element.isChecked()) {
                page.check(selector);
            } else if (!checked && element.isChecked()) {
                page.uncheck(selector);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to handle checkbox {}: {}", selector, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean handleRadio(String selector, String value) {
        try {
            browserManager.getPage().check(selector);
            return true;
        } catch (Exception e) {
            log.error("Failed to handle radio {}: {}", selector, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Boolean> validateForm(String formSelector) {
        var results = new HashMap<String, Boolean>();
        var fields = domAnalyzer.analyzeForm(formSelector);

        for (var field : fields) {
            if (field.required()) {
                var value = browserManager.getPage().querySelector(field.selector()) != null
                    ? browserManager.getPage().querySelector(field.selector()).inputValue()
                    : null;
                results.put(field.selector(), value != null && !value.trim().isEmpty());
            }
        }

        return results;
    }

    @Override
    public List<String> getUnfilledRequiredFields(String formSelector) {
        var unfilled = new java.util.ArrayList<String>();
        var validationResults = validateForm(formSelector);

        for (var entry : validationResults.entrySet()) {
            if (!entry.getValue()) {
                unfilled.add(entry.getKey());
            }
        }

        return unfilled;
    }

    @Override
    public boolean submitForm(String formSelector) {
        try {
            var page = browserManager.getPage();
            var submitButton = page.querySelector(formSelector + " button[type='submit'], " +
                formSelector + " input[type='submit'], " +
                formSelector + " button:has-text('Submit'), " +
                formSelector + " button:has-text('Apply')");

            if (submitButton != null) {
                submitButton.click();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to submit form: {}", e.getMessage());
            return false;
        }
    }
}
