package com.jobpilot.modules.ai.infrastructure.adapter;

import com.jobpilot.modules.ai.domain.model.*;
import com.jobpilot.modules.ai.domain.port.AIProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiAdapter implements AIProviderPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiAdapter.class);
    private static final String BASE_URL = "https://api.openai.com/v1";

    private final RestClient client;
    private final String apiKey;
    private final String defaultModel;

    public OpenAiAdapter(@Value("${ai.openai.api-key}") String apiKey,
                          @Value("${ai.openai.model:gpt-4}") String defaultModel) {
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.client = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public AiResponse generateText(AiRequest request) {
        var body = buildRequestBody(request);
        var start = System.currentTimeMillis();

        var response = client.post()
            .uri("/chat/completions")
            .body(body)
            .retrieve()
            .body(OpenAiResponse.class);

        var latency = System.currentTimeMillis() - start;
        var choice = response.choices().get(0);

        return new AiResponse(
            choice.message().content(),
            mapFinishReason(choice.finishReason()),
            new TokenUsage(response.usage().promptTokens(), response.usage().completionTokens(),
                response.usage().totalTokens()),
            response.model(),
            latency
        );
    }

    @Override
    public Flux<AiChunk> generateStream(AiRequest request) {
        var body = buildRequestBody(request);
        body.put("stream", true);

        return Flux.create(sink -> {
            client.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(new java.io.InputStream[1]);
            sink.complete();
        });
    }

    @Override
    public List<Float> generateEmbedding(String text) {
        var response = client.post()
            .uri("/embeddings")
            .body(Map.of("model", "text-embedding-3-small", "input", text))
            .retrieve()
            .body(OpenAiEmbeddingResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            return List.of();
        }
        return response.data().get(0).embedding();
    }

    @Override
    public int countTokens(String text) {
        var response = client.post()
            .uri("/tokenizers")
            .body(Map.of("model", defaultModel, "input", text))
            .retrieve()
            .body(OpenAiTokenCountResponse.class);

        return response != null ? response.tokenCount() : text.length() / 4;
    }

    @Override
    public String providerName() {
        return "openai";
    }

    private Map<String, Object> buildRequestBody(AiRequest request) {
        return Map.of(
            "model", request.model() != null ? request.model() : defaultModel,
            "messages", request.messages().stream()
                .map(m -> Map.of("role", m.role().name().toLowerCase(), "content", m.content()))
                .toList(),
            "temperature", request.temperature(),
            "max_tokens", request.maxTokens()
        );
    }

    private FinishReason mapFinishReason(String reason) {
        if (reason == null) return FinishReason.STOP;
        return switch (reason) {
            case "stop" -> FinishReason.STOP;
            case "length" -> FinishReason.LENGTH;
            case "content_filter" -> FinishReason.CONTENT_FILTER;
            case "tool_calls" -> FinishReason.TOOL_CALLS;
            default -> FinishReason.ERROR;
        };
    }

    private record OpenAiResponse(String id, String model, List<Choice> choices, Usage usage) {}
    private record Choice(Message message, String finishReason) {}
    private record Message(String role, String content) {}
    private record Usage(int promptTokens, int completionTokens, int totalTokens) {}
    private record OpenAiEmbeddingResponse(String model, List<EmbeddingData> data, Usage usage) {}
    private record EmbeddingData(int index, List<Float> embedding) {}
    private record OpenAiTokenCountResponse(int tokenCount) {}
}
