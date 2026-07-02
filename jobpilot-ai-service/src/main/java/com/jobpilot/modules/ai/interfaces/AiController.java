package com.jobpilot.modules.ai.interfaces;

import com.jobpilot.modules.ai.application.service.AiOrchestrationService;
import com.jobpilot.modules.ai.domain.model.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiOrchestrationService orchestrationService;

    public AiController(AiOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<AiResponse> generate(@RequestBody GenerateRequest request) {
        var aiRequest = toDomainRequest(request);
        var response = orchestrationService.generateText(request.useCase(), aiRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChunk> generateStream(@RequestBody GenerateRequest request) {
        var aiRequest = toDomainRequest(request);
        return orchestrationService.generateStream(request.useCase(), aiRequest);
    }

    @PostMapping("/embeddings")
    public ResponseEntity<List<Float>> embeddings(@RequestBody EmbeddingRequest request) {
        var embedding = orchestrationService.generateEmbedding(request.text());
        return ResponseEntity.ok(embedding);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ai-provider-layer"));
    }

    private AiRequest toDomainRequest(GenerateRequest request) {
        return AiRequest.builder()
            .model(request.model())
            .messages(request.messages().stream()
                .map(m -> new AiMessage(m.role(), m.content(), m.name()))
                .toList())
            .temperature(request.temperature() != null ? request.temperature() : 0.7)
            .maxTokens(request.maxTokens() != null ? request.maxTokens() : 2048)
            .build();
    }

    public record GenerateRequest(PromptUseCase useCase, String model, List<MessageDto> messages,
                                  Double temperature, Integer maxTokens) {}
    public record MessageDto(AiMessageRole role, String content, String name) {}
    public record EmbeddingRequest(String text) {}
}
