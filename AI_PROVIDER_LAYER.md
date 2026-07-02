# JobPilot AI — AI Provider Layer

**Version:** 1.0  
**Status:** Draft  
**Phase:** 10 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Abstract interface between business logic and LLM providers. Enables provider-swappable AI without changing any application code. Supports OpenAI, Anthropic, Ollama (local), and Google Gemini.

---

## 2. Architecture

```
┌──────────────────────────────────────────────┐
│              Application Layer               │
│  ResumeService  InterviewService  etc.       │
│         │              │                     │
│         ▼              ▼                     │
│  ┌──────────────────────────────────────┐   │
│  │      AiOrchestrationService          │   │
│  │  (provider selection, caching,       │   │
│  │   retry, cost tracking)             │   │
│  └──────────────┬───────────────────────┘   │
└─────────────────┼───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│            Domain Port (Interface)           │
│         AIProviderPort (com.jobpilot)        │
│  + generateText(AiRequest): AiResponse      │
│  + generateStream(AiRequest): Flux<AiChunk> │
│  + generateEmbedding(String): List<Float>   │
│  + countTokens(String): int                 │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Infrastructure Adapters             │
│                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │  OpenAI  │ │Anthropic │ │  Ollama  │   │
│  │  Adapter │ │ Adapter  │ │  Adapter │   │
│  └──────────┘ └──────────┘ └──────────┘   │
│  ┌──────────┐ ┌──────────────────────────┐│
│  │  Gemini  │ │CircuitBreakerAspect (AOP)││
│  │  Adapter │ └──────────────────────────┘│
│  └──────────┘                              │
└────────────────────────────────────────────┘
```

---

## 3. Provider Interface

```java
public interface AIProviderPort {
    AiResponse generateText(AiRequest request);
    Flux<AiChunk> generateStream(AiRequest request);
    List<Float> generateEmbedding(String text);
    int countTokens(String text);
}

// Domain objects (no framework imports):
public record AiRequest(String model, List<AiMessage> messages,
    double temperature, int maxTokens, List<String> stopSequences,
    ResponseFormat responseFormat, List<AiTool> tools) {}

public record AiMessage(AiMessageRole role, String content, String name) {}

public record AiResponse(String content, FinishReason finishReason,
    TokenUsage usage, String modelUsed, long latencyMs) {}

public record AiChunk(String content, FinishReason finishReason) {}

public record TokenUsage(int promptTokens, int completionTokens, int totalTokens) {}

public record AiTool(String name, String description, JsonSchema parameters) {}
```

---

## 4. Provider Adapters

### 4.1 OpenAI Adapter

```
Endpoint: https://api.openai.com/v1
Models: gpt-4, gpt-4-turbo, gpt-3.5-turbo, text-embedding-3-small (512d), text-embedding-3-large (1536d)
Auth: Bearer token (API key)
Streaming: Server-Sent Events
SDK: Spring AI OpenAiChatClient
```

### 4.2 Anthropic Adapter

```
Endpoint: https://api.anthropic.com/v1
Models: claude-3-opus-20240229, claude-3-sonnet-20240229, claude-3-haiku-20240307
Auth: x-api-key header
Streaming: Server-Sent Events
SDK: Spring AI AnthropicChatClient
```

### 4.3 Ollama Adapter

```
Endpoint: http://localhost:11434 (local) / http://ollama-service:11434 (docker)
Models: llama3, mixtral, codellama, mistral
Auth: None (local)
SDK: Spring AI OllamaChatClient
```

### 4.4 Gemini Adapter

```
Endpoint: https://generativelanguage.googleapis.com/v1
Models: gemini-1.5-pro, gemini-1.5-flash
Auth: API key query parameter
SDK: Google Vertex AI client
```

---

## 5. Provider Selection Strategy

| Use Case | Primary Provider | Fallback | Model Selection |
|----------|-----------------|----------|-----------------|
| Resume tailoring | OpenAI GPT-4 | Anthropic Claude 3 Opus | Need highest quality text generation |
| Resume scoring | Anthropic Claude 3 Sonnet | OpenAI GPT-4 | Good balance speed/quality |
| Cover letter gen | OpenAI GPT-4 | Claude 3 Sonnet | Creativity + formatting |
| Interview questions | OpenAI GPT-4 | Claude 3 Sonnet | Domain knowledge breadth |
| Answer scoring | Anthropic Claude 3 Haiku | GPT-3.5 | Fast, cheap, sufficient |
| Career path | GPT-3.5 | Claude 3 Haiku | Cost-optimized |
| Skills gap | Embedding (local) + GPT-3.5 | None | Vector similarity first, LLM for text |
| Networking msg | GPT-3.5 | Claude 3 Haiku | Short output, cost-sensitive |
| Semantic search | Local embedding (Ollama) | OpenAI embedding | Zero cost per query |

---

## 6. Caching Strategy

| Cache Key Pattern | TTL | Storage | Invalidation |
|-------------------|-----|---------|--------------|
| `ai:prompt:{sha256(prompt)}` | 24h | Redis | Manual flush on prompt template update |
| `ai:embedding:{sha256(text)}` | 7 days | Redis | LRU eviction |
| `ai:ats:resume:{resumeId}:job:{jobHash}` | 1h | Redis | On resume update |

---

## 7. Circuit Breaker

```
Per-provider circuit breaker (Resilience4j / custom AOP):
  - CLOSED: normal operation, requests pass
  - OPEN: fail fast (throw AiServiceUnavailableException), fallback to next provider
  - HALF_OPEN: allow single test request after timeout

Thresholds:
  - Failure rate: 50% in 60-second window
  - Minimum calls: 5
  - Open duration: 30 seconds
  - Timeout: 30s per request (text), 60s (streaming)
```

---

## 8. Token Tracking & Cost

```java
AiUsageLog {
    userId, useCase, provider, model,
    promptTokens, completionTokens, totalTokens,
    costMicroUsd,  // cost in micro-cents (1/1,000,000 USD)
    latencyMs,
    cacheHit: boolean,
    createdAt
}

// Cost calculation (configurable per model):
// gpt-4: $30/1M input, $60/1M output tokens
// claude-3-sonnet: $3/1M input, $15/1M output
// etc.
```

---

**End of AI Provider Layer v1.0**
