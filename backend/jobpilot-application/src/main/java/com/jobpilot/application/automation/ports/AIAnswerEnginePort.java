package com.jobpilot.application.automation.ports;

import java.util.List;
import java.util.Map;

public interface AIAnswerEnginePort {

    String generateAnswer(String question, String context, Map<String, String> profile);

    String generateAnswerForYesNo(String question, String context);

    String generateSalaryAnswer(String question, Integer minSalary, Integer maxSalary);

    String generateExperienceAnswer(String question, int yearsExperience);

    List<String> generateAnswers(List<QuestionDetectorPort.ApplicationQuestion> questions,
                                  Map<String, String> candidateProfile);

    default boolean isAvailable() {
        return true;
    }
}
