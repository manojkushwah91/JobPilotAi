package com.jobpilot.interfaces.rest.v1.ai;

import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiResumeScoringPort aiResumeScoringPort;
    private final AiSkillGapPort aiSkillGapPort;
    private final AiJobMatchPort aiJobMatchPort;
    private final AiProviderPort aiProviderPort;

    public AiController(AiResumeScoringPort aiResumeScoringPort, AiSkillGapPort aiSkillGapPort,
                        AiJobMatchPort aiJobMatchPort, AiProviderPort aiProviderPort) {
        this.aiResumeScoringPort = aiResumeScoringPort;
        this.aiSkillGapPort = aiSkillGapPort;
        this.aiJobMatchPort = aiJobMatchPort;
        this.aiProviderPort = aiProviderPort;
    }

    @RateLimited(capacity = 5)
    @PostMapping("/resume/score")
    public ResponseEntity<ApiResponse<AiResumeScoreResponse>> scoreResume(@RequestBody AiResumeScoreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiResumeScoringPort.scoreResume(request)));
    }

    @RateLimited(capacity = 5)
    @PostMapping("/resume/skill-gap")
    public ResponseEntity<ApiResponse<AiSkillGapResponse>> skillGap(@RequestBody AiSkillGapRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiSkillGapPort.analyzeSkillGap(request)));
    }

    @RateLimited(capacity = 5)
    @PostMapping("/job/match")
    public ResponseEntity<ApiResponse<AiJobMatchResponse>> matchJob(@RequestBody AiJobMatchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiJobMatchPort.matchJob(request)));
    }

    @RateLimited(capacity = 10)
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generate(@RequestBody Map<String, Object> request) {
        var prompt = request.getOrDefault("prompt", "").toString();
        var type = request.getOrDefault("type", "general").toString();
        var systemPrompt = request.getOrDefault("systemPrompt", "").toString();
        var userPrompt = request.getOrDefault("userPrompt", prompt).toString();
        var modelOverride = request.getOrDefault("model", "").toString();
        var temperature = Double.parseDouble(request.getOrDefault("temperature", 0.7).toString());
        var maxTokens = Integer.parseInt(request.getOrDefault("maxTokens", 500).toString());

        var content = aiProviderPort.executePrompt(
            systemPrompt.isBlank() ? "You are a helpful assistant." : systemPrompt,
            userPrompt,
            modelOverride.isBlank() ? null : modelOverride,
            temperature,
            maxTokens
        );

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "id", java.util.UUID.randomUUID().toString(),
            "content", content,
            "type", type,
            "model", modelOverride.isBlank() ? "ollama" : modelOverride,
            "usage", Map.of("promptTokens", 0, "completionTokens", 0, "totalTokens", 0)
        )));
    }

    @RateLimited(capacity = 10)
    @GetMapping(value = "/generate/stream", produces = "text/event-stream")
    public ResponseEntity<String> generateStreamGet(
            @RequestParam(defaultValue = "") String prompt) {
        return ResponseEntity.ok()
            .contentType(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
            .body("data: " + new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(Map.of(
                "content", "Streaming response for: " + prompt,
                "done", true
            )).toString() + "\n\n");
    }

    @RateLimited(capacity = 10)
    @PostMapping(value = "/generate/stream", produces = "text/event-stream")
    public ResponseEntity<String> generateStream(@RequestBody Map<String, Object> request) {
        var prompt = request.getOrDefault("prompt", "");
        return ResponseEntity.ok()
            .contentType(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
            .body("data: " + new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(Map.of(
                "content", "Streaming response for: " + prompt,
                "done", true
            )).toString() + "\n\n");
    }

    @RateLimited(capacity = 20)
    @PostMapping("/embeddings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> embeddings(@RequestBody Map<String, Object> request) {
        var input = request.getOrDefault("input", "");
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "object", "list",
            "data", List.of(Map.of("object", "embedding", "index", 0, "embedding", new float[1536])),
            "model", "text-embedding-ada-002",
            "usage", Map.of("promptTokens", 1, "totalTokens", 1)
        )));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "status", "available",
            "provider", "openai",
            "models", List.of("gpt-4", "gpt-3.5-turbo"),
            "latency", 0
        )));
    }
}
