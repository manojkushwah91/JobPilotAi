package com.jobpilot.modules.ai.infrastructure.adapter;

import com.jobpilot.modules.ai.domain.model.*;
import com.jobpilot.modules.ai.domain.port.AIProviderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GeminiAdapter implements AIProviderPort {

    private final RestClient client;
    private final String apiKey;

    public GeminiAdapter(@Value("${ai.gemini.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public AiResponse generateText(AiRequest request) {
        var body = buildRequestBody(request);

        var start = System.currentTimeMillis();
        var response = client.post()
            .uri("/models/gemini-1.5-pro:generateContent?key=" + apiKey)
            .body(body)
            .retrieve()
            .body(GeminiResponse.class);
        var latency = System.currentTimeMillis() - start;

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new RuntimeException("Empty response from Gemini");
        }

        var candidate = response.candidates().get(0);
        var content = candidate.content().parts().stream()
            .map(GeminiPart::text)
            .filter(t -> t != null)
            .collect(Collectors.joining());

        var usage = response.usageMetadata() != null ? response.usageMetadata() : new GeminiUsage(0, 0);

        return new AiResponse(
            content,
            FinishReason.STOP,
            new TokenUsage(usage.promptTokenCount(), usage.candidatesTokenCount(),
                usage.promptTokenCount() + usage.candidatesTokenCount()),
            "gemini-1.5-pro",
            latency
        );
    }

    @Override
    public Flux<AiChunk> generateStream(AiRequest request) {
        return Flux.empty();
    }

    @Override
    public List<Float> generateEmbedding(String text) {
        var response = client.post()
            .uri("/models/text-embedding-004:embedContent?key=" + apiKey)
            .body(Map.of("model", "models/text-embedding-004",
                "content", Map.of("parts", List.of(Map.of("text", text)))))
            .retrieve()
            .body(GeminiEmbeddingResponse.class);

        if (response == null || response.embedding() == null) return List.of();
        return response.embedding().values();
    }

    @Override
    public int countTokens(String text) {
        var response = client.post()
            .uri("/models/gemini-1.5-pro:countTokens?key=" + apiKey)
            .body(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", text))))))
            .retrieve()
            .body(GeminiTokenCount.class);
        return response != null ? response.totalTokens() : text.length() / 4;
    }

    @Override
    public String providerName() {
        return "gemini";
    }

    private Map<String, Object> buildRequestBody(AiRequest request) {
        var contents = request.messages().stream()
            .filter(m -> m.role() != AiMessageRole.SYSTEM)
            .map(m -> Map.of("role", mapRole(m.role()),
                "parts", List.of(Map.of("text", m.content()))))
            .toList();

        var systemText = request.messages().stream()
            .filter(m -> m.role() == AiMessageRole.SYSTEM)
            .map(AiMessage::content)
            .collect(Collectors.joining("\n"));

        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("contents", contents);
        if (!systemText.isBlank()) {
            body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemText))));
        }
        body.put("generationConfig", Map.of(
            "temperature", request.temperature(),
            "maxOutputTokens", request.maxTokens()
        ));
        return body;
    }

    private String mapRole(AiMessageRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "model";
            default -> "user";
        };
    }

    private record GeminiResponse(List<GeminiCandidate> candidates, GeminiUsage usageMetadata) {}
    private record GeminiCandidate(GeminiContent content, String finishReason) {}
    private record GeminiContent(List<GeminiPart> parts, String role) {}
    private record GeminiPart(String text) {}
    private record GeminiUsage(int promptTokenCount, int candidatesTokenCount) {}
    private record GeminiEmbeddingResponse(GeminiEmbedding embedding) {}
    private record GeminiEmbedding(List<Float> values) {}
    private record GeminiTokenCount(int totalTokens) {}
}
