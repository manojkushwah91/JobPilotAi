package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.QuestionDetectorPort;
import com.jobpilot.application.automation.ports.DomAnalyzerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlaywrightQuestionDetector implements QuestionDetectorPort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightQuestionDetector.class);

    private final PlaywrightBrowserManager browserManager;
    private final PlaywrightDomAnalyzer domAnalyzer;

    public PlaywrightQuestionDetector(PlaywrightBrowserManager browserManager, PlaywrightDomAnalyzer domAnalyzer) {
        this.browserManager = browserManager;
        this.domAnalyzer = domAnalyzer;
    }

    @Override
    public List<ApplicationQuestion> detectQuestions() {
        var questions = new ArrayList<ApplicationQuestion>();
        var page = browserManager.getPage();

        var questionSelectors = List.of(
            ".application-question",
            "[data-testid='question']",
            ".form-group",
            ".field-group",
            ".question-container",
            ".form-field"
        );

        for (var selector : questionSelectors) {
            var elements = page.querySelectorAll(selector);
            for (var element : elements) {
                var questionText = extractQuestionText(element);
                if (questionText != null && !questionText.isEmpty()) {
                    var questionType = detectQuestionType(element);
                    var required = element.querySelector("[required]") != null ||
                        element.innerText().contains("*");
                    var options = extractOptions(element);

                    questions.add(new ApplicationQuestion(
                        selector,
                        questionText,
                        questionType.name(),
                        required,
                        "",
                        options,
                        ""
                    ));
                }
            }
        }

        log.debug("Detected {} questions", questions.size());
        return questions;
    }

    @Override
    public ApplicationQuestion detectQuestion(String selector) {
        var page = browserManager.getPage();
        var element = page.querySelector(selector);
        if (element == null) return null;

        var questionText = extractQuestionText(element);
        var questionType = detectQuestionType(element);
        var required = element.querySelector("[required]") != null;
        var options = extractOptions(element);

        return new ApplicationQuestion(
            selector,
            questionText,
            questionType.name(),
            required,
            "",
            options,
            ""
        );
    }

    @Override
    public boolean hasQuestions() {
        return detectQuestions().size() > 0;
    }

    @Override
    public int questionCount() {
        return detectQuestions().size();
    }

    private String extractQuestionText(com.microsoft.playwright.ElementHandle element) {
        var label = element.querySelector("label");
        if (label != null) return label.innerText().trim();

        var heading = element.querySelector("h3, h4, .question-text, .field-label");
        if (heading != null) return heading.innerText().trim();

        return element.innerText().trim().split("\n")[0];
    }

    private ApplicationQuestion.QuestionType detectQuestionType(com.microsoft.playwright.ElementHandle element) {
        var input = element.querySelector("input, select, textarea");
        if (input == null) return ApplicationQuestion.QuestionType.UNKNOWN;

        var type = input.getAttribute("type");
        var tagName = input.evaluate("el => el.tagName.toLowerCase()").toString();

        if ("select".equals(tagName)) return ApplicationQuestion.QuestionType.DROPDOWN;
        if ("textarea".equals(tagName)) return ApplicationQuestion.QuestionType.TEXTAREA;
        if ("radio".equals(type)) return ApplicationQuestion.QuestionType.RADIO;
        if ("checkbox".equals(type)) return ApplicationQuestion.QuestionType.CHECKBOX;
        if ("file".equals(type)) return ApplicationQuestion.QuestionType.FILE_UPLOAD;
        if ("date".equals(type)) return ApplicationQuestion.QuestionType.DATE;
        if ("number".equals(type)) return ApplicationQuestion.QuestionType.NUMBER;
        if ("email".equals(type)) return ApplicationQuestion.QuestionType.EMAIL;
        if ("tel".equals(type)) return ApplicationQuestion.QuestionType.PHONE;

        return ApplicationQuestion.QuestionType.TEXT_INPUT;
    }

    private List<String> extractOptions(com.microsoft.playwright.ElementHandle element) {
        var options = new ArrayList<String>();

        var select = element.querySelector("select");
        if (select != null) {
            var optionElements = select.querySelectorAll("option");
            for (var option : optionElements) {
                options.add(option.innerText().trim());
            }
        }

        var radioOptions = element.querySelectorAll("input[type='radio']");
        for (var radio : radioOptions) {
            var label = radio.evaluate("el => el.closest('label')?.innerText || el.nextSibling?.textContent || ''")
                .toString().trim();
            if (!label.isEmpty()) options.add(label);
        }

        return options;
    }
}
