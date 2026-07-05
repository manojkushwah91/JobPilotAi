package com.jobpilot.interfaces.rest.v1.ai;

import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiResumeScoringPort aiResumeScoringPort;
    private final AiSkillGapPort aiSkillGapPort;
    private final AiJobMatchPort aiJobMatchPort;
    private final AiProviderPort aiProviderPort;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        var content = aiProviderPort.executePrompt(
            "You are a helpful assistant.", prompt, null, 0.7, 500);
        var sseData = "data: " + objectMapper.valueToTree(Map.of(
            "content", content,
            "done", true
        )).toString() + "\n\n";
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(sseData);
    }

    @RateLimited(capacity = 10)
    @PostMapping(value = "/generate/stream", produces = "text/event-stream")
    public ResponseEntity<String> generateStream(@RequestBody Map<String, Object> request) {
        var prompt = request.getOrDefault("prompt", "").toString();
        var systemPrompt = request.getOrDefault("systemPrompt", "").toString();
        var modelOverride = request.getOrDefault("model", "").toString();
        var temperature = Double.parseDouble(request.getOrDefault("temperature", 0.7).toString());
        var maxTokens = Integer.parseInt(request.getOrDefault("maxTokens", 500).toString());

        var content = aiProviderPort.executePrompt(
            systemPrompt.isBlank() ? "You are a helpful assistant." : systemPrompt,
            prompt,
            modelOverride.isBlank() ? null : modelOverride,
            temperature,
            maxTokens
        );
        var sseData = "data: " + objectMapper.valueToTree(Map.of(
            "content", content,
            "done", true
        )).toString() + "\n\n";
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(sseData);
    }

    @RateLimited(capacity = 20)
    @PostMapping("/embeddings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> embeddings(@RequestBody Map<String, Object> request) {
        var input = request.getOrDefault("input", "").toString();
        var model = request.getOrDefault("model", "nomic-embed-text").toString();
        var embedding = getOllamaEmbedding(input, model);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "object", "list",
            "data", List.of(Map.of("object", "embedding", "index", 0, "embedding", embedding)),
            "model", model,
            "usage", Map.of("promptTokens", 1, "totalTokens", 1)
        )));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        try {
            var rt = getRestTemplate();
            var response = rt.getForObject(ollamaBaseUrl + "/api/tags", JsonNode.class);
            if (response != null && response.has("models")) {
                var models = response.get("models");
                var modelNames = new java.util.ArrayList<String>();
                models.forEach(m -> modelNames.add(m.get("name").asText()));
                return ResponseEntity.ok(ApiResponse.ok(Map.of(
                    "status", "available",
                    "provider", "ollama",
                    "models", modelNames,
                    "latency", 0
                )));
            }
        } catch (Exception e) {
            // Ollama not available
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "status", "unavailable",
            "provider", "ollama",
            "models", List.of(),
            "latency", 0
        )));
    }

    private float[] getOllamaEmbedding(String text, String model) {
        try {
            var rt = getRestTemplate();
            var body = Map.of("model", model, "prompt", text);
            var response = rt.postForObject(ollamaBaseUrl + "/api/embeddings", body, JsonNode.class);
            if (response != null && response.has("embedding")) {
                var arr = response.get("embedding");
                var result = new float[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    result[i] = arr.get(i).floatValue();
                }
                return result;
            }
        } catch (Exception e) {
            // Fallback to zero vector if Ollama is unavailable
        }
        return new float[1536];
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate != null) {
            return restTemplate;
        }
        return new RestTemplate();
    }
}
