package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.AgentMemory;
import com.jobpilot.domain.agent.MemoryId;
import com.jobpilot.domain.agent.MemoryType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentMemoryRepository {

    AgentMemory save(AgentMemory memory);

    Optional<AgentMemory> findById(MemoryId memoryId);

    Optional<AgentMemory> findByUserIdAndTypeAndKey(UUID userId, MemoryType type, String key);

    List<AgentMemory> findByUserId(UUID userId);

    List<AgentMemory> findByUserIdAndType(UUID userId, MemoryType type);

    List<AgentMemory> findActiveByUserId(UUID userId);

    void delete(MemoryId memoryId);
}
