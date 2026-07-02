package com.jobpilot.modules.ai.infrastructure.adapter;

import com.jobpilot.modules.ai.domain.model.*;
import com.jobpilot.modules.ai.domain.port.AIProviderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class OllamaAdapter implements AIProviderPort {

    private final RestClient client;
    private final String defaultModel;

    public OllamaAdapter(@Value("${ai.ollama.base-url:http://localhost:11434}") String baseUrl,
                          @Value("${ai.ollama.model:llama3}") String defaultModel) {
        this.defaultModel = defaultModel;
        this.client = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public AiResponse generateText(AiRequest request) {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("model", request.model() != null ? request.model() : defaultModel);
        body.put("prompt", buildPrompt(request));
        body.put("stream", false);
        body.put("options", Map.of("temperature", request.temperature(),
            "num_predict", request.maxTokens()));

        var start = System.currentTimeMillis();
        var response = client.post()
            .uri("/api/generate")
            .body(body)
            .retrieve()
            .body(OllamaResponse.class);
        var latency = System.currentTimeMillis() - start;

        if (response == null) {
            throw new RuntimeException("Empty response from Ollama");
        }

        return new AiResponse(
            response.response() != null ? response.response() : "",
            FinishReason.STOP,
            new TokenUsage(response.promptEvalCount() != null ? response.promptEvalCount() : 0,
                response.evalCount() != null ? response.evalCount() : 0, 0),
            response.model(),
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
            .uri("/api/embeddings")
            .body(Map.of("model", "nomic-embed-text", "prompt", text))
            .retrieve()
            .body(OllamaEmbeddingResponse.class);

        return response != null ? response.embedding() : List.of();
    }

    @Override
    public int countTokens(String text) {
        return text.length() / 4;
    }

    @Override
    public String providerName() {
        return "ollama";
    }

    private String buildPrompt(AiRequest request) {
        var sb = new StringBuilder();
        for (var msg : request.messages()) {
            var role = switch (msg.role()) {
                case SYSTEM -> "System";
                case USER -> "User";
                case ASSISTANT -> "Assistant";
                default -> "User";
            };
            sb.append(role).append(": ").append(msg.content()).append("\n");
        }
        sb.append("Assistant: ");
        return sb.toString();
    }

    private record OllamaResponse(String model, String response, Integer promptEvalCount, Integer evalCount) {}
    private record OllamaEmbeddingResponse(List<Float> embedding) {}
}
