package com.jobpilot.modules.ai.application.service;

import com.jobpilot.modules.ai.domain.model.AiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiCacheService {

    private static final Logger log = LoggerFactory.getLogger(AiCacheService.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public AiCacheService() {
        startEvictionThread();
    }

    public void put(String key, AiResponse response) {
        cache.put(key, new CacheEntry(response, System.currentTimeMillis() + 86_400_000));
    }

    public Optional<AiResponse> get(String key) {
        var entry = cache.get(key);
        if (entry == null) return Optional.empty();
        if (System.currentTimeMillis() > entry.expiresAt()) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.response());
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    private void startEvictionThread() {
        var thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000);
                    var now = System.currentTimeMillis();
                    cache.entrySet().removeIf(e -> now > e.getValue().expiresAt());
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ai-cache-evictor");
        thread.setDaemon(true);
        thread.start();
    }

    private record CacheEntry(AiResponse response, long expiresAt) {}
}
