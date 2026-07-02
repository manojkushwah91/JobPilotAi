package com.jobpilot.modules.ai.domain.port;

import com.jobpilot.modules.ai.domain.model.AiChunk;
import com.jobpilot.modules.ai.domain.model.AiRequest;
import com.jobpilot.modules.ai.domain.model.AiResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AIProviderPort {
    AiResponse generateText(AiRequest request);

    Flux<AiChunk> generateStream(AiRequest request);

    List<Float> generateEmbedding(String text);

    int countTokens(String text);

    String providerName();
}
