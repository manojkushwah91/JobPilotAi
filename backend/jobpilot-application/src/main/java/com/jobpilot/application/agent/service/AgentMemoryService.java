package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.AgentMemoryRepository;
import com.jobpilot.domain.agent.AgentMemory;
import com.jobpilot.domain.agent.MemoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AgentMemoryService {

    private static final Logger log = LoggerFactory.getLogger(AgentMemoryService.class);

    private final AgentMemoryRepository memoryRepository;

    public AgentMemoryService(AgentMemoryRepository memoryRepository) {
        this.memoryRepository = memoryRepository;
    }

    public AgentMemory store(UUID userId, MemoryType type, String key, String value) {
        var existing = memoryRepository.findByUserIdAndTypeAndKey(userId, type, key);
        if (existing.isPresent()) {
            var memory = existing.get();
            memory.updateValue(value);
            memoryRepository.save(memory);
            log.debug("Updated memory {} for user {}", key, userId);
            return memory;
        }
        var memory = AgentMemory.create(userId, type, key, value);
        memoryRepository.save(memory);
        log.debug("Stored memory {} for user {}", key, userId);
        return memory;
    }

    public AgentMemory storeWithMetadata(UUID userId, MemoryType type, String key,
                                          String value, Map<String, Object> metadata) {
        var memory = store(userId, type, key, value);
        memoryRepository.save(memory);
        return memory;
    }

    public AgentMemory recall(UUID userId, MemoryType type, String key) {
        var memory = memoryRepository.findByUserIdAndTypeAndKey(userId, type, key);
        if (memory.isPresent()) {
            var m = memory.get();
            m.access();
            memoryRepository.save(m);
            return m;
        }
        return null;
    }

    public List<AgentMemory> getAllMemories(UUID userId) {
        return memoryRepository.findActiveByUserId(userId);
    }

    public List<AgentMemory> getMemoriesByType(UUID userId, MemoryType type) {
        return memoryRepository.findByUserIdAndType(userId, type);
    }

    public void forget(UUID userId, MemoryType type, String key) {
        var memory = memoryRepository.findByUserIdAndTypeAndKey(userId, type, key);
        memory.ifPresent(m -> {
            m.deactivate();
            memoryRepository.save(m);
            log.debug("Deactivated memory {} for user {}", key, userId);
        });
    }

    public boolean hasMemory(UUID userId, MemoryType type, String key) {
        return memoryRepository.findByUserIdAndTypeAndKey(userId, type, key).isPresent();
    }
}
