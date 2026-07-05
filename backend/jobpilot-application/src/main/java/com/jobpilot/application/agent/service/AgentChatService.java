package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.AiProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentChatService {

    private static final Logger log = LoggerFactory.getLogger(AgentChatService.class);

    private static final String SYSTEM_PROMPT = """
        You are JobPilot, an AI-powered job search agent assistant.
        You help users with job searching, application tracking, resume optimization, and career guidance.
        You can assist with:
        - Finding and matching job opportunities
        - Tracking application status and mission progress
        - Pausing, resuming, or stopping job search missions
        - Resume and cover letter optimization advice
        - Salary research and negotiation tips
        - Company research and skip preferences

        When users mention specific commands like "find jobs", "show applications", "pause mission",
        "resume mission", "stop mission", "skip company", or "salary", respond naturally and helpfully.
        Explain what you can do for them and guide them accordingly.

        Keep your responses concise (under 5 sentences) and conversational.
        If you don't have enough context about a user's missions or preferences, ask clarifying questions.
        Always be supportive and professional.
        """;

    private final AiProviderPort aiProvider;
    private final Map<UUID, List<ChatMessage>> conversationHistory = new ConcurrentHashMap<>();

    public AgentChatService(AiProviderPort aiProvider) {
        this.aiProvider = aiProvider;
    }

    public ChatResponse processMessage(UUID userId, String message) {
        log.info("Processing chat message from user {}: {}", userId, message);

        var history = conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>());
        history.add(new ChatMessage("user", message));

        String responseText;

        if (!aiProvider.isAvailable()) {
            responseText = "I'm currently unable to process your request. " +
                "The AI service is temporarily unavailable. Please try again in a moment.";
        } else {
            responseText = callAiProvider(userId, message, history);
        }

        history.add(new ChatMessage("assistant", responseText));
        trimHistory(history);

        return new ChatResponse(responseText, "info");
    }

    public List<ChatMessage> getConversationHistory(UUID userId) {
        return List.copyOf(conversationHistory.getOrDefault(userId, List.of()));
    }

    public void clearConversationHistory(UUID userId) {
        conversationHistory.remove(userId);
    }

    private String callAiProvider(UUID userId, String message, List<ChatMessage> history) {
        var contextBuilder = new StringBuilder();
        var start = Math.max(0, history.size() - 6);
        for (int i = start; i < history.size() - 1; i++) {
            var msg = history.get(i);
            contextBuilder.append(msg.role()).append(": ").append(msg.content()).append("\n");
        }
        contextBuilder.append("user: ").append(message);

        try {
            var response = aiProvider.executePromptWithContext(
                SYSTEM_PROMPT,
                message,
                contextBuilder.toString(),
                null,
                0.7,
                300
            );

            if (response == null || response.isBlank()) {
                return "I'm not sure how to respond to that. Could you rephrase your question?";
            }

            return response.trim();
        } catch (Exception e) {
            log.error("Error calling AI provider for user {}: {}", userId, e.getMessage());
            return "I encountered an error processing your request. " +
                "Please try again, or rephrase your question.";
        }
    }

    private void trimHistory(List<ChatMessage> history) {
        if (history.size() > 20) {
            history.subList(0, history.size() - 20).clear();
        }
    }

    public record ChatResponse(String response, String type) {}
    public record ChatMessage(String role, String content) {}
}
