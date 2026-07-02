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
public class AnthropicAdapter implements AIProviderPort {

    private static final String BASE_URL = "https://api.anthropic.com/v1";

    private final RestClient client;

    public AnthropicAdapter(@Value("${ai.anthropic.api-key}") String apiKey) {
        this.client = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader("x-api-key", apiKey)
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public AiResponse generateText(AiRequest request) {
        var system = extractSystemPrompt(request);
        var body = buildRequestBody(request, system);

        var start = System.currentTimeMillis();
        var response = client.post()
            .uri("/messages")
            .body(body)
            .retrieve()
            .body(AnthropicResponse.class);
        var latency = System.currentTimeMillis() - start;

        if (response == null || response.content() == null || response.content().isEmpty()) {
            throw new RuntimeException("Empty response from Anthropic");
        }

        var content = response.content().stream()
            .map(c -> c.text())
            .filter(t -> t != null)
            .collect(Collectors.joining());

        return new AiResponse(
            content,
            FinishReason.STOP,
            new TokenUsage(response.usage().inputTokens(), response.usage().outputTokens(),
                response.usage().inputTokens() + response.usage().outputTokens()),
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
        throw new UnsupportedOperationException("Anthropic does not support embeddings");
    }

    @Override
    public int countTokens(String text) {
        var response = client.post()
            .uri("/messages/count_tokens")
            .body(Map.of("model", "claude-3-haiku-20240307", "messages",
                List.of(Map.of("role", "user", "content", text))))
            .retrieve()
            .body(AnthropicTokenCount.class);
        return response != null ? response.inputTokens() : text.length() / 4;
    }

    @Override
    public String providerName() {
        return "anthropic";
    }

    private String extractSystemPrompt(AiRequest request) {
        return request.messages().stream()
            .filter(m -> m.role() == AiMessageRole.SYSTEM)
            .map(AiMessage::content)
            .collect(Collectors.joining("\n"));
    }

    private Map<String, Object> buildRequestBody(AiRequest request, String system) {
        var messages = request.messages().stream()
            .filter(m -> m.role() != AiMessageRole.SYSTEM)
            .map(m -> Map.of("role", m.role().name().toLowerCase(), "content", m.content()))
            .toList();

        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("model", "claude-3-sonnet-20240229");
        body.put("max_tokens", request.maxTokens());
        body.put("messages", messages);
        if (!system.isBlank()) body.put("system", system);
        body.put("temperature", request.temperature());
        return body;
    }

    private record AnthropicResponse(String id, String model, List<ContentBlock> content, Usage usage) {}
    private record ContentBlock(String type, String text) {}
    private record Usage(int inputTokens, int outputTokens) {}
    private record AnthropicTokenCount(int inputTokens) {}
}
