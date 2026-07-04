# JobPilot AI v2.0 — AI Provider Layer

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Abstract interface between business logic and LLM providers. Enables provider-swappable AI without changing any application code. **Ollama is the default provider (local, offline-first).** Cloud providers (OpenAI, Anthropic, Google Gemini) are optional plugins.

---

## 2. Architecture

```
┌──────────────────────────────────────────────┐
│              Agent Runtime                   │
│  AI Tools (ResumeParser, JobAnalyzer, etc.)  │
│         │              │                     │
│         ▼              ▼                     │
│  ┌──────────────────────────────────────┐   │
│  │      AiProviderPort (Interface)      │   │
│  │  + generateText(AiRequest): AiResponse│   │
│  │  + generateStream(AiRequest): Flux   │   │
│  │  + generateEmbedding(String): List    │   │
│  │  + countTokens(String): int          │   │
│  │  + isAvailable(): boolean            │   │
│  └──────────────┬───────────────────────┘   │
└─────────────────┼───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Infrastructure Adapters             │
│                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │  Ollama  │ │  OpenAI  │ │ Anthropic│   │
│  │  Adapter │ │  Adapter │ │ Adapter  │   │
│  │  (DEFAULT)│ │ (OPTIONAL)│ │ (OPTIONAL)│   │
│  └──────────┘ └──────────┘ └──────────┘   │
│  ┌──────────┐ ┌──────────────────────────┐│
│  │  Gemini  │ │CircuitBreakerAspect (AOP)││
│  │  Adapter │ └──────────────────────────┘│
│  │ (OPTIONAL)│                            ││
│  └──────────┘                              │
└────────────────────────────────────────────┘
```

---

## 3. Provider Interface

```java
public interface AiProvider {
    
    AiResponse generateText(AiRequest request);
    
    Flux<AiChunk> generateStream(AiRequest request);
    
    List<Float> generateEmbedding(String text);
    
    int countTokens(String text);
    
    boolean isAvailable();
    
    String providerName();
}

// Domain objects (no framework imports):
public record AiRequest(
    String systemPrompt,
    String userPrompt,
    double temperature,
    int maxTokens,
    List<String> stopSequences
) {}

public record AiResponse(
    String content,
    String model,
    int promptTokens,
    int completionTokens,
    long latencyMs
) {}

public record AiChunk(
    String content,
    boolean done
) {}
```

---

## 4. Provider Adapters

### 4.1 Ollama Provider (DEFAULT)

```
Endpoint: http://localhost:11434 (local)
Models: llama3, qwen2.5, mistral, deepseek, gemma
Auth: None (local)
SDK: Custom HTTP client
Auto-Detection: Checks http://localhost:11434 on startup
```

**Configuration:**
```yaml
ai:
  provider:
    default: ollama
    ollama:
      base-url: http://localhost:11434
      model: llama3
```

**Auto-Detection:**
```java
@Component
public class OllamaAutoDetector {
    
    @EventListener(ApplicationReadyEvent.class)
    public void detectOllama() {
        var ollamaAvailable = checkOllamaAvailability();
        
        if (!ollamaAvailable) {
            log.warn("Ollama not detected. Please install Ollama from https://ollama.ai");
            log.warn("After installation, run: ollama pull llama3");
        } else {
            log.info("Ollama detected and available");
        }
    }
}
```

### 4.2 OpenAI Provider (OPTIONAL)

```
Endpoint: https://api.openai.com/v1
Models: gpt-4, gpt-4-turbo, gpt-3.5-turbo
Auth: Bearer token (API key, user-provided, encrypted)
SDK: Spring AI OpenAiChatClient
Opt-in: User must explicitly configure API key
```

**Configuration:**
```yaml
ai:
  provider:
    default: ollama
    enable-cloud-fallback: false
    openai:
      api-key: ${OPENAI_API_KEY}  # User-provided, encrypted
      model: gpt-4
```

### 4.3 Anthropic Provider (OPTIONAL)

```
Endpoint: https://api.anthropic.com/v1
Models: claude-3-opus, claude-3-sonnet, claude-3-haiku
Auth: x-api-key header (user-provided, encrypted)
SDK: Spring AI AnthropicChatClient
Opt-in: User must explicitly configure API key
```

**Configuration:**
```yaml
ai:
  provider:
    default: ollama
    enable-cloud-fallback: false
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}  # User-provided, encrypted
      model: claude-3-sonnet
```

### 4.4 Gemini Provider (OPTIONAL)

```
Endpoint: https://generativelanguage.googleapis.com/v1
Models: gemini-1.5-pro, gemini-1.5-flash
Auth: API key query parameter (user-provided, encrypted)
SDK: Google Vertex AI client
Opt-in: User must explicitly configure API key
```

**Configuration:**
```yaml
ai:
  provider:
    default: ollama
    enable-cloud-fallback: false
    gemini:
      api-key: ${GEMINI_API_KEY}  # User-provided, encrypted
      model: gemini-1.5-pro
```

---

## 5. Provider Selection Strategy

### 5.1 Default Strategy

**Ollama is always the default provider.** The system is designed to work completely offline with Ollama.

### 5.2 Cloud Fallback (Optional)

Cloud providers can be configured as fallback if Ollama is unavailable, but this is **opt-in only**. The user must explicitly enable cloud fallback and provide API keys.

**Configuration:**
```yaml
ai:
  provider:
    default: ollama
    enable-cloud-fallback: true  # Opt-in only
    fallback-order: openai, anthropic, gemini
```

### 5.3 Use Case Model Selection

| Use Case | Ollama Model | Cloud Fallback (if enabled) |
|----------|--------------|---------------------------|
| Resume parsing | llama3 | gpt-4 |
| Job analysis | llama3 | gpt-4 |
| Resume tailoring | llama3 | gpt-4 |
| Cover letter generation | llama3 | gpt-4 |
| Answer generation | llama3 | gpt-4 |
| Job ranking | llama3 | gpt-4 |
| Scam detection | llama3 | gpt-4 |
| Skill gap analysis | llama3 | gpt-4 |

---

## 6. Caching Strategy

| Cache Key Pattern | TTL | Storage | Invalidation |
|-------------------|-----|---------|--------------|
| `ai:prompt:{sha256(prompt)}` | 24h | Redis | Manual flush on prompt template update |
| `ai:embedding:{sha256(text)}` | 7 days | Redis | LRU eviction |
| `ai:job:{jobId}:analysis` | 1h | Redis | On job update |

---

## 7. Circuit Breaker

```
Per-provider circuit breaker (Resilience4j):
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

## 8. Token Tracking

```java
AiUsageLog {
    userId, useCase, provider, model,
    promptTokens, completionTokens, totalTokens,
    latencyMs,
    cacheHit: boolean,
    createdAt
}

// Note: Cost tracking is not applicable for Ollama (local, free)
// Cost tracking is optional for cloud providers (if enabled)
```

---

## 9. Security

### 9.1 API Key Management

- Cloud AI API keys are encrypted at rest (AES-256)
- API keys are never logged
- API keys are user-provided and can be revoked at any time
- API keys are stored in environment variables or Vault

### 9.2 Data Privacy

- **Ollama (Default):** All data stays on user's machine. No data leaves the local environment.
- **Cloud AI (Optional):** Data is transmitted to cloud provider only if user explicitly opts in and provides API key.

---

## 10. Ollama Setup

### 10.1 Installation

**Linux:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

**macOS:**
```bash
brew install ollama
```

**Windows:**
```powershell
winget install ollama
```

### 10.2 Pull Models

```bash
# Pull default model
ollama pull llama3

# Pull additional models (optional)
ollama pull qwen2.5
ollama pull mistral
ollama pull deepseek
ollama pull gemma
```

### 10.3 Verify Installation

```bash
# Check Ollama is running
ollama list

# Test inference
ollama run llama3 "What is 2+2?"
```

---

## 11. Error Handling

### 11.1 Ollama Unavailable

If Ollama is not detected on startup:
1. Log warning message
2. Guide user through installation
3. If cloud fallback is disabled (default), system operates in degraded mode (no AI features)
4. If cloud fallback is enabled, attempt to use cloud provider

### 11.2 Cloud Provider Unavailable

If cloud provider is configured but unavailable:
1. Log error
2. Circuit breaker opens
3. Fallback to next provider in fallback order
4. If all providers unavailable, throw AiServiceUnavailableException

---

## 12. Testing

### 12.1 Unit Tests

Mock AI provider for deterministic tests:

```java
@Component
@Profile("test")
public class MockAiProvider implements AiProvider {
    
    @Override
    public AiResponse generateText(AiRequest request) {
        return AiResponse.builder()
            .content("Mock response")
            .model("mock-model")
            .promptTokens(10)
            .completionTokens(20)
            .latencyMs(100)
            .build();
    }
    
    // ... other methods
}
```

### 12.2 Integration Tests

Test with real Ollama (optional, requires Ollama running):

```java
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OLLAMA_ENABLED", matches = "true")
class OllamaProviderIntegrationTest {
    
    @Test
    void shouldGenerateText() {
        var request = AiRequest.builder()
            .systemPrompt("You are a helpful assistant")
            .userPrompt("What is 2+2?")
            .build();
        
        var response = ollamaProvider.generateText(request);
        
        assertThat(response.content()).isNotEmpty();
    }
}
```

---

**End of AI Provider Layer v2.0**
