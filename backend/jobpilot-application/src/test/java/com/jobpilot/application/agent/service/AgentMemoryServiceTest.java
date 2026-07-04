package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.AgentMemoryRepository;
import com.jobpilot.domain.agent.AgentMemory;
import com.jobpilot.domain.agent.MemoryId;
import com.jobpilot.domain.agent.MemoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentMemoryServiceTest {

    @Mock
    private AgentMemoryRepository memoryRepository;

    private AgentMemoryService memoryService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        memoryService = new AgentMemoryService(memoryRepository);
        testUserId = UUID.randomUUID();
    }

    @Test
    void store_shouldCreateNewMemoryWhenNotExists() {
        when(memoryRepository.findByUserIdAndTypeAndKey(testUserId, MemoryType.REJECTED_COMPANY, "TCS"))
            .thenReturn(Optional.empty());
        when(memoryRepository.save(any(AgentMemory.class))).thenReturn(
            AgentMemory.create(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "Poor work culture"));

        var result = memoryService.store(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "Poor work culture");

        assertNotNull(result);
        assertEquals("TCS", result.memoryKey());
        assertEquals("Poor work culture", result.value());
        verify(memoryRepository).save(any(AgentMemory.class));
    }

    @Test
    void store_shouldUpdateExistingMemoryWhenExists() {
        var existingMemory = AgentMemory.create(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "Old reason");
        when(memoryRepository.findByUserIdAndTypeAndKey(testUserId, MemoryType.REJECTED_COMPANY, "TCS"))
            .thenReturn(Optional.of(existingMemory));
        when(memoryRepository.save(any(AgentMemory.class))).thenReturn(existingMemory);

        var result = memoryService.store(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "New reason");

        assertEquals("New reason", result.value());
        verify(memoryRepository).save(any(AgentMemory.class));
    }

    @Test
    void recall_shouldReturnMemoryAndIncrementAccessCount() {
        var memory = AgentMemory.create(testUserId, MemoryType.SKILL_PREFERENCE, "Java", "Expert");
        when(memoryRepository.findByUserIdAndTypeAndKey(testUserId, MemoryType.SKILL_PREFERENCE, "Java"))
            .thenReturn(Optional.of(memory));
        when(memoryRepository.save(any(AgentMemory.class))).thenReturn(memory);

        var result = memoryService.recall(testUserId, MemoryType.SKILL_PREFERENCE, "Java");

        assertNotNull(result);
        assertEquals("Java", result.memoryKey());
        assertEquals(1, result.accessCount());
        assertNotNull(result.lastAccessedAt());
    }

    @Test
    void recall_shouldReturnNullWhenNotExists() {
        when(memoryRepository.findByUserIdAndTypeAndKey(testUserId, MemoryType.SKILL_PREFERENCE, "Java"))
            .thenReturn(Optional.empty());

        var result = memoryService.recall(testUserId, MemoryType.SKILL_PREFERENCE, "Java");

        assertNull(result);
    }

    @Test
    void getAllMemories_shouldReturnAllActiveMemories() {
        var memories = List.of(
            AgentMemory.create(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "Reason"),
            AgentMemory.create(testUserId, MemoryType.SKILL_PREFERENCE, "Java", "Expert")
        );
        when(memoryRepository.findActiveByUserId(testUserId)).thenReturn(memories);

        var result = memoryService.getAllMemories(testUserId);

        assertEquals(2, result.size());
    }

    @Test
    void getMemoriesByType_shouldReturnMemoriesOfType() {
        var memories = List.of(
            AgentMemory.create(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "Reason"),
            AgentMemory.create(testUserId, MemoryType.REJECTED_COMPANY, "Infosys", "Reason")
        );
        when(memoryRepository.findByUserIdAndType(testUserId, MemoryType.REJECTED_COMPANY))
            .thenReturn(memories);

        var result = memoryService.getMemoriesByType(testUserId, MemoryType.REJECTED_COMPANY);

        assertEquals(2, result.size());
    }

    @Test
    void forget_shouldDeactivateMemory() {
        var memory = AgentMemory.create(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "Reason");
        when(memoryRepository.findByUserIdAndTypeAndKey(testUserId, MemoryType.REJECTED_COMPANY, "TCS"))
            .thenReturn(Optional.of(memory));
        when(memoryRepository.save(any(AgentMemory.class))).thenReturn(memory);

        memoryService.forget(testUserId, MemoryType.REJECTED_COMPANY, "TCS");

        assertFalse(memory.isActive());
        verify(memoryRepository).save(any(AgentMemory.class));
    }

    @Test
    void hasMemory_shouldReturnTrueWhenMemoryExists() {
        when(memoryRepository.findByUserIdAndTypeAndKey(testUserId, MemoryType.SKILL_PREFERENCE, "Java"))
            .thenReturn(Optional.of(AgentMemory.create(testUserId, MemoryType.SKILL_PREFERENCE, "Java", "Expert")));

        assertTrue(memoryService.hasMemory(testUserId, MemoryType.SKILL_PREFERENCE, "Java"));
    }

    @Test
    void hasMemory_shouldReturnFalseWhenMemoryNotExists() {
        when(memoryRepository.findByUserIdAndTypeAndKey(testUserId, MemoryType.SKILL_PREFERENCE, "Java"))
            .thenReturn(Optional.empty());

        assertFalse(memoryService.hasMemory(testUserId, MemoryType.SKILL_PREFERENCE, "Java"));
    }
}
