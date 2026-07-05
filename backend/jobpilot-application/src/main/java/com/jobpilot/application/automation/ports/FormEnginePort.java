package com.jobpilot.application.automation.ports;

import java.util.List;
import java.util.Map;

public interface FormEnginePort {

    boolean fillField(String selector, String value, String fieldType);

    boolean selectDropdown(String selector, String value);

    boolean uploadFile(String selector, String filePath);

    boolean answerQuestion(String questionSelector, String answer, String questionType);

    boolean handleCheckbox(String selector, boolean checked);

    boolean handleRadio(String selector, String value);

    Map<String, Boolean> validateForm(String formSelector);

    List<String> getUnfilledRequiredFields(String formSelector);

    boolean submitForm(String formSelector);

    default Map<String, String> extractFormData(String formSelector) {
        return Map.of();
    }
}
