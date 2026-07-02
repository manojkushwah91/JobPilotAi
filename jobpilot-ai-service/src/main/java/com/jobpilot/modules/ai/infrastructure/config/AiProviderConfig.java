package com.jobpilot.modules.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProviderConfig {

    private Map<String, ProviderProperties> providers;
    private CacheProperties cache;

    public Map<String, ProviderProperties> getProviders() { return providers; }
    public void setProviders(Map<String, ProviderProperties> providers) { this.providers = providers; }
    public CacheProperties getCache() { return cache; }
    public void setCache(CacheProperties cache) { this.cache = cache; }

    public static class ProviderProperties {
        private String apiKey;
        private String model;
        private String baseUrl;
        private int maxRetries = 3;
        private int timeoutSeconds = 30;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    public static class CacheProperties {
        private int promptTtlHours = 24;
        private int embeddingTtlDays = 7;
        private int maxSize = 10_000;

        public int getPromptTtlHours() { return promptTtlHours; }
        public void setPromptTtlHours(int promptTtlHours) { this.promptTtlHours = promptTtlHours; }
        public int getEmbeddingTtlDays() { return embeddingTtlDays; }
        public void setEmbeddingTtlDays(int embeddingTtlDays) { this.embeddingTtlDays = embeddingTtlDays; }
        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
    }
}
