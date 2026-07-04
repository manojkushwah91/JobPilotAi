package com.jobpilot.infrastructure.persistence.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentMemoryJpaRepository extends JpaRepository<AgentMemoryJpaEntity, UUID> {

    Optional<AgentMemoryJpaEntity> findByUserIdAndMemoryTypeAndKey(UUID userId, com.jobpilot.domain.agent.MemoryType memoryType, String key);

    List<AgentMemoryJpaEntity> findByUserId(UUID userId);

    List<AgentMemoryJpaEntity> findByUserIdAndMemoryType(UUID userId, com.jobpilot.domain.agent.MemoryType memoryType);

    List<AgentMemoryJpaEntity> findByUserIdAndActive(UUID userId, boolean active);
}
