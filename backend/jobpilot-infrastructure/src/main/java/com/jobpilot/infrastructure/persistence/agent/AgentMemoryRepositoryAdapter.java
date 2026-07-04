package com.jobpilot.infrastructure.persistence.agent;

import com.jobpilot.application.agent.ports.AgentMemoryRepository;
import com.jobpilot.domain.agent.AgentMemory;
import com.jobpilot.domain.agent.MemoryId;
import com.jobpilot.domain.agent.MemoryType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgentMemoryRepositoryAdapter implements AgentMemoryRepository {

    private final AgentMemoryJpaRepository jpaRepository;

    public AgentMemoryRepositoryAdapter(AgentMemoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AgentMemory save(AgentMemory memory) {
        var entity = AgentMemoryJpaEntity.fromDomain(memory);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AgentMemory> findById(MemoryId memoryId) {
        return jpaRepository.findById(memoryId.value()).map(AgentMemoryJpaEntity::toDomain);
    }

    @Override
    public Optional<AgentMemory> findByUserIdAndTypeAndKey(UUID userId, MemoryType type, String key) {
        return jpaRepository.findByUserIdAndMemoryTypeAndKey(userId, type, key)
            .map(AgentMemoryJpaEntity::toDomain);
    }

    @Override
    public List<AgentMemory> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
            .map(AgentMemoryJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<AgentMemory> findByUserIdAndType(UUID userId, MemoryType type) {
        return jpaRepository.findByUserIdAndMemoryType(userId, type).stream()
            .map(AgentMemoryJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<AgentMemory> findActiveByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndActive(userId, true).stream()
            .map(AgentMemoryJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void delete(MemoryId memoryId) {
        jpaRepository.deleteById(memoryId.value());
    }
}
