package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.AIAnswerEnginePort;
import com.jobpilot.application.automation.ports.QuestionDetectorPort;
import com.jobpilot.application.agent.ports.AiProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DefaultAIAnswerEngine implements AIAnswerEnginePort {

    private static final Logger log = LoggerFactory.getLogger(DefaultAIAnswerEngine.class);

    private final AiProviderPort aiProviderPort;

    public DefaultAIAnswerEngine(AiProviderPort aiProviderPort) {
        this.aiProviderPort = aiProviderPort;
    }

    @Override
    public String generateAnswer(String question, String context, Map<String, String> profile) {
        log.debug("Generating answer for question: {}", question);

        var profileStr = profile != null ? profile.toString() : "";
        var prompt = String.format(
            """
            You are helping a job applicant answer an application question.
            
            Context: %s
            Profile: %s
            
            Question: %s
            
            Provide a concise, professional answer in 1-2 sentences.
            """,
            context, profileStr, question
        );

        return aiProviderPort.executePrompt("You are a job application assistant.", prompt, "default", 0.7, 200);
    }

    @Override
    public String generateAnswerForYesNo(String question, String context) {
        var prompt = String.format(
            "Answer this yes/no question concisely: %s\nContext: %s",
            question, context
        );
        var answer = aiProviderPort.executePrompt("You are a job application assistant.", prompt, "default", 0.3, 50);
        return answer.toLowerCase().startsWith("yes") ? "Yes" : "No";
    }

    @Override
    public String generateSalaryAnswer(String question, Integer minSalary, Integer maxSalary) {
        var prompt = String.format(
            "Provide a salary expectation for this question: %s\nSalary range: $%d - $%d",
            question, minSalary, maxSalary
        );
        return aiProviderPort.executePrompt("You are a job application assistant.", prompt, "default", 0.3, 50);
    }

    @Override
    public String generateExperienceAnswer(String question, int yearsExperience) {
        var prompt = String.format(
            "Answer this experience question with %d years: %s",
            yearsExperience, question
        );
        return aiProviderPort.executePrompt("You are a job application assistant.", prompt, "default", 0.3, 50);
    }

    @Override
    public List<String> generateAnswers(List<QuestionDetectorPort.ApplicationQuestion> questions,
                                         Map<String, String> candidateProfile) {
        var answers = new ArrayList<String>();

        for (var question : questions) {
            var answer = generateAnswer(question.text(), "", candidateProfile);
            answers.add(answer);
        }

        return answers;
    }
}
