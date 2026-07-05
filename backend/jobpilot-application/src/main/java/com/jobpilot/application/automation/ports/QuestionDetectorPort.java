package com.jobpilot.application.automation.ports;

import java.util.List;

public interface QuestionDetectorPort {

    List<ApplicationQuestion> detectQuestions();

    ApplicationQuestion detectQuestion(String selector);

    boolean hasQuestions();

    int questionCount();

    record ApplicationQuestion(
        String selector,
        String text,
        String type,
        boolean required,
        String currentValue,
        List<String> options,
        String context
    ) {
        public enum QuestionType {
            TEXT_INPUT,
            TEXTAREA,
            DROPDOWN,
            RADIO,
            CHECKBOX,
            FILE_UPLOAD,
            DATE,
            NUMBER,
            PHONE,
            EMAIL,
            YES_NO,
            SALARY,
            UNKNOWN
        }
    }
}
