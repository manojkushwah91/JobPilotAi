package com.jobpilot.modules.ai.application.service;

import com.jobpilot.modules.ai.domain.model.*;
import com.jobpilot.modules.ai.domain.port.AIProviderPort;
import com.jobpilot.modules.ai.infrastructure.tracking.AiUsageLog;
import com.jobpilot.modules.ai.infrastructure.tracking.UsageTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Service
public class AiOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AiOrchestrationService.class);

    private final ProviderSelector providerSelector;
    private final AiCacheService aiCache;
    private final UsageTracker usageTracker;

    public AiOrchestrationService(ProviderSelector providerSelector,
                                   AiCacheService aiCache,
                                   UsageTracker usageTracker) {
        this.providerSelector = providerSelector;
        this.aiCache = aiCache;
        this.usageTracker = usageTracker;
    }

    public AiResponse generateText(PromptUseCase useCase, AiRequest request) {
        var cacheKey = buildCacheKey(useCase, request);
        var cached = aiCache.get(cacheKey);
        if (cached.isPresent()) {
            log.info("AI cache hit for useCase={}", useCase);
            return cached.get();
        }

        var provider = providerSelector.select(useCase);
        try {
            var start = System.currentTimeMillis();
            var response = executeWithFallback(provider, providerSelector.fallback(useCase), useCase, request);
            var latency = System.currentTimeMillis() - start;

            aiCache.put(cacheKey, response);
            usageTracker.track(new AiUsageLog(useCase, provider.providerName(),
                response.modelUsed(), response.usage(), latency, false));
            return response;
        } catch (Exception e) {
            log.error("All AI providers failed for useCase={}", useCase, e);
            throw new AiServiceException("All AI providers unavailable", e);
        }
    }

    public Flux<AiChunk> generateStream(PromptUseCase useCase, AiRequest request) {
        var provider = providerSelector.select(useCase);
        return provider.generateStream(request);
    }

    public List<Float> generateEmbedding(String text) {
        return providerSelector.select(PromptUseCase.SKILLS_GAP).generateEmbedding(text);
    }

    private AiResponse executeWithFallback(AIProviderPort primary, AIProviderPort fallback,
                                            PromptUseCase useCase, AiRequest request) {
        try {
            return primary.generateText(request);
        } catch (Exception e) {
            log.warn("Primary provider {} failed for useCase={}, attempting fallback",
                primary.providerName(), useCase, e);
            if (fallback != null) {
                return fallback.generateText(request);
            }
            throw e;
        }
    }

    private String buildCacheKey(PromptUseCase useCase, AiRequest request) {
        var messages = request.messages().stream()
            .map(m -> m.role() + ":" + m.content())
            .reduce((a, b) -> a + "|" + b)
            .orElse("");
        var input = useCase + ":" + request.model() + ":" + messages;
        return "ai:prompt:" + sha256(input);
    }

    private String sha256(String input) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            var bytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var hex = new StringBuilder();
            for (var b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
